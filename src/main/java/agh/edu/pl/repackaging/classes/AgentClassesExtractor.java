/* (C)2021 */
package agh.edu.pl.repackaging.classes;

import static agh.edu.pl.utils.ZipEntryCreator.createZipEntryFromFile;

import agh.edu.pl.repackaging.config.FolderNames;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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
  private List<String> excludedEntriesPrefixes = new ArrayList<>();

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

  private void copyMainFile(ZipOutputStream zout) throws IOException {
    JarFile jarFile = null;
    try {
      jarFile = new JarFile(this.mainFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (jarFile == null) {
      System.err.println("Error while getting project jar file.");
      return;
    }
    try {
      for (Enumeration<JarEntry> enums = jarFile.entries(); enums.hasMoreElements(); ) {
        JarEntry entry = enums.nextElement();
        ZipEntry outEntry = new ZipEntry(entry);
        zout.putNextEntry(outEntry);
        InputStream in = jarFile.getInputStream(entry);
        in.transferTo(zout);
        in.close();
        zout.closeEntry();
      }
      jarFile.close();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      FileUtils.deleteDirectory(lastFolder);
    }
  }

  private boolean shouldIncludeEntry(String entryName) {
    return excludedEntriesPrefixes.stream().noneMatch(entryName::startsWith);
  }

  public void addOpenTelemetryFolders() throws IOException {
    try {
      File finalDir = new File(folderNames.getFinalFolder());
      finalDir.mkdir();

      String pattern = Pattern.quote(System.getProperty("file.separator"));
      String[] outFileNameParts = mainFile.getName().split(pattern);
      final File outFile =
          new File(folderNames.getFinalFolder(), outFileNameParts[outFileNameParts.length - 1]);
      final ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(outFile));
      zout.setMethod(ZipOutputStream.STORED);
      copyMainFile(zout);

      for (Enumeration<JarEntry> enums = agentJar.entries(); enums.hasMoreElements(); ) {
        JarEntry entry = enums.nextElement();
        String entryName = entry.getName();
        if ((entryName.startsWith("inst/") || entryName.startsWith("io/"))
            && !entry.isDirectory()
            && shouldIncludeEntry(entryName)) {
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
                System.err.println("Error while copying OpenTelemetry classes to JAR.");
                return;
              }
            }
            InputStream in = agentJar.getInputStream(entry);
            in.transferTo(zout);
            in.close();
            zout.closeEntry();
          }
        }
      }
      zout.close();
      agentJar.close();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      FileUtils.deleteDirectory(folderNames.getOpenTelemetryClassesPackage());
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
    tmpFile.getParentFile().mkdirs();
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
