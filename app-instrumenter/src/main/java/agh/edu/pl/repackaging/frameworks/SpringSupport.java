/* (C)2021 */
package agh.edu.pl.repackaging.frameworks;

import agh.edu.pl.repackaging.config.FolderNames;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import static agh.edu.pl.utils.ZipEntryCreator.createZipEntryFromFile;

public class SpringSupport implements FrameworkSupport {

  private final String prefix = "BOOT-INF/classes/";
  private final FolderNames folderNames = FolderNames.getInstance();

  @Override
  public String getPrefix() {
    return prefix;
  }

  @Override
  public void copyMainClassesWithoutPrefix(JarFile inputJarFile, String outputFolder, String mainFilePath) {
    File outputDir = new File(outputFolder);
    if (!outputDir.mkdir() && !outputDir.exists()) {
      System.err.println(
              "The output directory could not be created. Please make sure you have permissions required to create a directory.");
      return;
    }
    String pattern = Pattern.quote(System.getProperty("file.separator"));
    String[] outFileNameParts = mainFilePath.split(pattern);
    final File outFile =
            new File(outputDir, outFileNameParts[outFileNameParts.length - 1]);
    ZipOutputStream zout;
    try {
      zout = new ZipOutputStream(new FileOutputStream(outFile));
    } catch (FileNotFoundException exception) {
      System.err.println(
              "Could not create output stream for JAR file, because file does not exist.");
      return;
    }
    zout.setMethod(ZipOutputStream.STORED);
      for (Enumeration<JarEntry> enums = inputJarFile.entries(); enums.hasMoreElements(); ) {
        JarEntry entry = enums.nextElement();
        ZipEntry outEntry = new ZipEntry(entry);
        try {
          if(entry.getName().startsWith("BOOT-INF/") && !entry.isDirectory()) {
            copyMainClassWithoutPrefix(entry, zout, inputJarFile);
          }
          else {
            zout.putNextEntry(outEntry);
            InputStream in = inputJarFile.getInputStream(entry);
            in.transferTo(zout);
            in.close();
            zout.closeEntry();
          }
        } catch (IOException exception) {
          System.err.println(
                  "Error while copying entry " + entry.getName() + " from main JAR to temporary file.");
        }
      }
      try {
        inputJarFile.close();
      } catch (IOException exception) {
        System.err.println("Main JAR was not closed properly.");
      }
  }

  private void copyMainClassWithoutPrefix(JarEntry entry, ZipOutputStream zout, JarFile jarFile) throws IOException {
    File tmpFile = copySingleEntryFromAgentFile(entry, jarFile);
    String newEntryPath = entry.getName().replace(prefix, "");
    createZipEntry(zout, tmpFile, newEntryPath);
  }

  private File copySingleEntryFromAgentFile(JarEntry entry, JarFile jarFile) throws IOException {
    File tmpFile = new File(folderNames.getFrameworkSupportMainClassAfterInstrumentation() + '/' + entry.getName());
    if (!tmpFile.getParentFile().mkdirs() && !tmpFile.getParentFile().exists()) {
      System.err.println(
              "Temporary directory for " + entry.getName() + " was not created properly.");
    }
    Files.copy(jarFile.getInputStream(entry), tmpFile.toPath());
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

  @Override
  public void addPrefixToMainClasses() {}
}
