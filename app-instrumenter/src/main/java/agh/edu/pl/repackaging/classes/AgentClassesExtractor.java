/* (C)2021 */
package agh.edu.pl.repackaging.classes;

import static agh.edu.pl.utils.ZipEntryCreator.copySingleEntryFromJar;
import static agh.edu.pl.utils.ZipEntryCreator.createZipEntryFromFile;

import agh.edu.pl.repackaging.config.FolderNames;
import agh.edu.pl.repackaging.frameworks.FrameworkSupport;
import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentClassesExtractor {
  private final File mainFile;
  private JarFile agentJar;
  private final String lastFolder;
  private final FolderNames folderNames = FolderNames.getInstance();
  private FrameworkSupport frameworkSupport;
  private final Logger logger = LoggerFactory.getLogger(AgentClassesExtractor.class);

  public AgentClassesExtractor(File mainFile, String agentPath, String lastFolder) {
    this.mainFile = mainFile;
    this.lastFolder = lastFolder;
    try {
      agentJar = new JarFile(agentPath);
    } catch (IOException e) {
      logger.error("Problem occurred while getting agent JAR file.");
    }
  }

  private void copyMainFile(ZipOutputStream zout, FrameworkSupport frameworkSupport) {
    JarFile jarFile;
    try {
      jarFile = new JarFile(this.mainFile);
    } catch (IOException e) {
      logger.error(
          "Problem occurred while getting project JAR file "
              + mainFile.getPath()
              + ".Make sure you have defined JAR packaging in pom.xml.");
      return;
    }
    try {
      for (Enumeration<JarEntry> enums = jarFile.entries(); enums.hasMoreElements(); ) {
        JarEntry entry = enums.nextElement();
        ZipEntry outEntry = new ZipEntry(entry);
        try {
          if (frameworkSupport != null
              && !entry.isDirectory()
              && frameworkSupport.getFilesToRepackage().contains(entry.getName())) {
            copyMainClassWithPrefix(entry, zout, jarFile, frameworkSupport);
          } else {
            zout.putNextEntry(outEntry);
            InputStream in = jarFile.getInputStream(entry);
            in.transferTo(zout);
            in.close();
            zout.closeEntry();
          }
        } catch (IOException exception) {
          logger.error(
              "Error while copying entry " + entry.getName() + " from main JAR to temporary file.");
        }
      }
      try {
        jarFile.close();
      } catch (IOException exception) {
        logger.error("Main JAR was not closed properly.");
      }
    } finally {
      try {
        FileUtils.deleteDirectory(lastFolder);
      } catch (IOException exception) {
        logger.error(
            "Temporary directory required for adding Agent classes process was not deleted properly.");
      }
    }
  }

  public void addOpenTelemetryFolders(FrameworkSupport frameworkSupport, String suffix) {
    try {
      File finalDir = new File(folderNames.getFinalFolder());
      this.frameworkSupport = frameworkSupport;
      if (!finalDir.mkdir() && !finalDir.exists()) {
        logger.error(
            "The output directory could not be created. Please make sure you have permissions required to create a directory.");
        return;
      }

      String pattern = Pattern.quote(System.getProperty("file.separator"));
      String[] outFileNameParts = mainFile.getName().split(pattern);
      String fileName = outFileNameParts[outFileNameParts.length - 1];
      int lastDotIndex = fileName.lastIndexOf('.');
      final File outFile =
          new File(
              folderNames.getFinalFolder(),
              String.format(
                  "%s%s%s",
                  fileName.substring(0, lastDotIndex), suffix, fileName.substring(lastDotIndex)));
      ZipOutputStream zout;
      try {
        zout = new ZipOutputStream(new FileOutputStream(outFile));
      } catch (FileNotFoundException exception) {
        logger.error(
            "Could not create output stream for JAR file, because file "
                + outFile.getPath()
                + " does not exist.");
        return;
      }
      zout.setMethod(ZipOutputStream.STORED);
      copyMainFile(zout, frameworkSupport);
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
            } else if (frameworkSupport != null) {
              copySingleEntryWithFrameworkSupport(entry, zout);
            } else {
              ZipEntry outEntry = new ZipEntry(entry);
              try {
                zout.putNextEntry(outEntry);
              } catch (ZipException e) {
                if (e.getMessage().contains("duplicate")) continue;
                else {
                  logger.error(
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
            logger.error(
                "Error while copying OpenTelemetry file " + entryName + "to main JAR.");
            return;
          }
        }
      }
      try {
        zout.close();
        agentJar.close();
      } catch (IOException exception) {
        logger.error("Agent JAR file/output stream was not closed properly.");
      }
    } finally {
      try {
        FileUtils.deleteDirectory(folderNames.getOpenTelemetryClassesPackage());
        FileUtils.deleteDirectory(folderNames.getFrameworkSupportFolder());
      } catch (IOException exception) {
        logger.error(
            "Temporary directory required for adding Agent classes to main JAR process was not deleted properly.");
        exception.printStackTrace();
      }
    }
  }

  private void copySingleEntryWithFrameworkSupport(JarEntry entry, ZipOutputStream zout)
      throws IOException {
    File tmpFile =
        copySingleEntryFromJar(entry, agentJar, folderNames.getOpenTelemetryClassesPackage());
    String prefix = frameworkSupport.getClassesPrefix();
    String newEntryPath = prefix + entry.getName();
    createZipEntryFromFile(zout, tmpFile, newEntryPath);
  }

  private void copySingleInstFolderFile(JarEntry entry, ZipOutputStream zout) throws IOException {
    File tmpFile =
        copySingleEntryFromJar(entry, agentJar, folderNames.getOpenTelemetryClassesPackage());
    String prefix = frameworkSupport != null ? frameworkSupport.getClassesPrefix() : "";
    String newEntryPath = entry.getName().replace("inst/", prefix);
    createZipEntryFromFile(zout, tmpFile, newEntryPath);
  }

  private void copyMainClassWithPrefix(
      JarEntry entry, ZipOutputStream zout, JarFile jarFile, FrameworkSupport frameworkSupport)
      throws IOException {
    File tmpFile =
        copySingleEntryFromJar(entry, jarFile, folderNames.getOpenTelemetryClassesPackage());
    String newEntryPath = frameworkSupport.getClassesPrefix() + entry.getName();
    createZipEntryFromFile(zout, tmpFile, newEntryPath);
  }

  private void copySingleClassdataFile(JarEntry entry, ZipOutputStream zout) throws IOException {
    File tmpFile =
        copySingleEntryFromJar(entry, agentJar, folderNames.getOpenTelemetryClassesPackage());
    String prefix = frameworkSupport != null ? frameworkSupport.getClassesPrefix() : "";
    String newEntryPath = entry.getName().replace(".classdata", ".class");
    newEntryPath = newEntryPath.replace("inst/", prefix);

    createZipEntryFromFile(zout, tmpFile, newEntryPath);
  }
}
