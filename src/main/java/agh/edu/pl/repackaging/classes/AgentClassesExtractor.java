/* (C)2021 */
package agh.edu.pl.repackaging.classes;

import static agh.edu.pl.utils.ZipEntryCreator.createZipEntryFromFile;

import agh.edu.pl.repackaging.config.FolderNames;
import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;
import org.codehaus.plexus.util.FileUtils;

public class AgentClassesExtractor {
  private final File mainFile;
  private JarFile agentJar;
  private final String lastFolder;
  private final FolderNames folderNames = FolderNames.getInstance();

  public AgentClassesExtractor(File mainFile, String agentPath, String lastFolder) {
    this.mainFile = mainFile;
    this.lastFolder = lastFolder;
    try {
      agentJar =
          new JarFile(
              agentPath.replace(folderNames.getInstrumentedOtelJarPackage() + File.separator, ""));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void copyMainFile(ZipOutputStream zout) {
    JarFile jarFile;
    try {
      jarFile = new JarFile(this.mainFile);
    } catch (IOException e) {
      System.err.println(
          "Problem occurred while getting project JAR file. Make sure you have defined JAR packaging in pom.xml.");
      return;
    }
    try {
      for (Enumeration<JarEntry> enums = jarFile.entries(); enums.hasMoreElements(); ) {
        JarEntry entry = enums.nextElement();
        ZipEntry outEntry = new ZipEntry(entry);
        try {
          zout.putNextEntry(outEntry);
          InputStream in = jarFile.getInputStream(entry);
          in.transferTo(zout);
          in.close();
          zout.closeEntry();
        } catch (IOException exception) {
          System.err.println(
              "Error while copying entry " + entry.getName() + " from main JAR to temporary file.");
        }
        try {
          jarFile.close();
        } catch (IOException exception) {
          System.err.println("Main JAR was not closed properly.");
        }
      }
    } finally {
      try {
        FileUtils.deleteDirectory(lastFolder);
      } catch (IOException exception) {
        System.err.println(
            "Temporary directory required for adding Agent classes process was not deleted properly.");
      }
    }
  }

  public void addOpenTelemetryFolders() {
    try {
      File finalDir = new File(folderNames.getFinalFolder());
      finalDir.mkdir();

      String pattern = Pattern.quote(System.getProperty("file.separator"));
      String[] outFileNameParts = mainFile.getName().split(pattern);
      final File outFile =
          new File(folderNames.getFinalFolder(), outFileNameParts[outFileNameParts.length - 1]);
      ZipOutputStream zout;
      try {
        zout = new ZipOutputStream(new FileOutputStream(outFile));
      } catch (FileNotFoundException exception) {
        System.err.println(
            "Could not create output stream for JAR file, because file does not exist.");
        return;
      }
      zout.setMethod(ZipOutputStream.STORED);
      copyMainFile(zout);

      for (Enumeration<JarEntry> enums = agentJar.entries(); enums.hasMoreElements(); ) {
        JarEntry entry = enums.nextElement();
        String entryName = entry.getName();
        if ((entryName.startsWith("inst/") || entryName.startsWith("io/"))
            && !entry.isDirectory()) {
          try {
            if (entryName.endsWith(".classdata")) {
              copySingleClassdataFile(entry, zout);
            } else if (entryName.startsWith("inst/")) {
              copySingleInstFolderFile(entry, zout);
            } else {
              ZipEntry outEntry = new ZipEntry(entry);
              try {
                zout.putNextEntry(outEntry);
              } catch (ZipException e) {
                if (e.getMessage().contains("duplicate")) continue;
                else {
                  System.err.println(
                      "Error while copying OpenTelemetry file " + entryName + "to main JAR.");
                  return;
                }
              }
              InputStream in = agentJar.getInputStream(entry);
              in.transferTo(zout);
              in.close();
              zout.closeEntry();
            }
          } catch (IOException exception) {
            System.err.println(
                "Error while copying OpenTelemetry file " + entryName + "to main JAR.");
            return;
          }
        }
      }
      try {
        zout.close();
        agentJar.close();
      } catch (IOException exception) {
        System.err.println("Agent JAR file/output stream was not closed properly.");
      }
    } finally {
      try {
        FileUtils.deleteDirectory(folderNames.getOpenTelemetryClassesPackage());
      } catch (IOException exception) {
        System.err.println(
            "Temporary directory required for adding Agent classes to main JAR process was not deleted properly.");
      }
    }
  }

  private void copySingleInstFolderFile(JarEntry entry, ZipOutputStream zout) throws IOException {
    File tmpFile = copySingleEntryFromAgentFile(entry);
    String newEntryPath = entry.getName().replace("inst/", "");
    createZipEntry(zout, tmpFile, newEntryPath);
  }

  private void copySingleClassdataFile(JarEntry entry, ZipOutputStream zout) throws IOException {
    File tmpFile = copySingleEntryFromAgentFile(entry);
    String newEntryPath = entry.getName().replace(".classdata", ".class").replace("inst/", "");
    createZipEntry(zout, tmpFile, newEntryPath);
  }

  private File copySingleEntryFromAgentFile(JarEntry entry) throws IOException {
    File tmpFile = new File(folderNames.getOpenTelemetryClassesPackage() + '/' + entry.getName());
    if (!tmpFile.getParentFile().mkdirs()) {
      System.err.println(
          "Temporary directory for " + entry.getName() + " was not created properly.");
    }
    Files.copy(agentJar.getInputStream(entry), tmpFile.toPath());
    return tmpFile;
  }

  private void createZipEntry(ZipOutputStream zout, File file, String pathToFile)
      throws IOException {
    try {
      createZipEntryFromFile(zout, file, pathToFile);
    } catch (ZipException e) {
      if (!e.getMessage().contains("duplicate")) {
        System.err.println("Error while copying OpenTelemetry classes to JAR.");
      }
    }
  }
}
