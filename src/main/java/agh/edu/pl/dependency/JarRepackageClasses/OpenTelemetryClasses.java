/* (C)2021 */
package agh.edu.pl.dependency.JarRepackageClasses;

import static agh.edu.pl.dependency.JarRepackager.OTEL_TMP;

import agh.edu.pl.dependency.JarRepackager;
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

public class OpenTelemetryClasses {
  private final File mainFile;
  private JarFile agentJar;
  private final String lastFolder;
  String ADD_OPENTELEMETRY_CLASSES = "./FINAL";
  String TMP_FOLDER = "./TMP_CLASSES";

  public OpenTelemetryClasses(File mainFile, String agentPath, String lastFolder) {
    this.mainFile = mainFile;
    this.lastFolder = lastFolder;
    try {
      agentJar = new JarFile(agentPath.replace(OTEL_TMP + '/', ""));
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

  public void addOpenTelemetryFolders() throws IOException {
    try {
      File finalDir = new File(ADD_OPENTELEMETRY_CLASSES);
      finalDir.mkdir();
      String pattern = Pattern.quote(System.getProperty("file.separator"));
      String[] outFileNameParts = mainFile.getName().split(pattern);
      final File outFile =
          new File(ADD_OPENTELEMETRY_CLASSES, outFileNameParts[outFileNameParts.length - 1]);
      final ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(outFile));
      zout.setMethod(ZipOutputStream.STORED);
      copyMainFile(zout);
      for (Enumeration<JarEntry> enums = agentJar.entries(); enums.hasMoreElements(); ) {
        JarEntry entry = enums.nextElement();
        if ((entry.getName().startsWith("inst/") || entry.getName().startsWith("io/"))
            && !entry.isDirectory()) {
          if (entry.getName().startsWith("inst/io/opentelemetry/javaagent/instrumentation/")
              && entry.getName().endsWith(".classdata")) {
            File tmpFile = new File(TMP_FOLDER + '/' + entry.getName());
            tmpFile.getParentFile().mkdirs();
            Files.copy(agentJar.getInputStream(entry), tmpFile.toPath());
            String newEntryPath =
                entry.getName().replace(".classdata", ".class").replace("inst/", "");
            createZipEntry(zout, tmpFile, newEntryPath);
          } else if (entry.getName().startsWith("inst/")) {
            File tmpFile = new File(TMP_FOLDER + '/' + entry.getName());
            tmpFile.getParentFile().mkdirs();
            Files.copy(agentJar.getInputStream(entry), tmpFile.toPath());
            String newEntryPath = entry.getName().replace("inst/", "");
            createZipEntry(zout, tmpFile, newEntryPath);
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
      FileUtils.deleteDirectory(TMP_FOLDER);
    }
  }

  private void createZipEntry(ZipOutputStream zout, File file, String pathToFile)
      throws IOException {
    try {
      JarRepackager.createZipEntryFromFile(zout, file, pathToFile);
    } catch (ZipException e) {
      if (!e.getMessage().contains("duplicate")) {
        System.err.println("Error while copying OpenTelemetry classes to JAR.");
      }
    }
  }
}
