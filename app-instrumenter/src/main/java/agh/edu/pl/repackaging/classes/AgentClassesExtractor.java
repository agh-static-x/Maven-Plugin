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

/**
 * Contains methods to extract and add OpenTelemetry javaagent classes to another JAR file.
 */
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

  /**
   * Copies classes from instrumented JAR file to the output JAR file. Adds prefix specific for framework if
   * necessary.
   * If error occurred while getting the instrumented JAR, the error is logged.
   * If error occurred while copying entry from instrumented JAR to zip output stream, the error is logged.
   *
   * @param zout ZipOutputStream for file the main JAR file should be copied to
   * @param frameworkSupport FrameworkSupport object with methods to check whether the prefix specific for framework
   *                         should be added to classes
   * @see FrameworkSupport
   * @see ZipOutputStream
   */
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
              "Error while copying entry " + entry.getName() + " from instrumented JAR to zip output stream.");
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

  /**
   * Adds classes from OpenTelemetry javaagent JAR to instrumented JAR. Creates new JAR files, then copies the
   * classes from the instrumented JAR. Adds classes from javaagent JAR, removes the shading and replaces classdata
   * file extension with class file extension.
   * If the output directory can't be created, the error is logged.
   * If the ZipOutputStream for new file can't be created, the error is logged.
   * If the file from OpenTelemetry JAR can't be copied, the error is logged.
   *
   * @param frameworkSupport FrameworkSupport object with methods to check whether the prefix specific for framework
   *    *                    should be added to classes
   * @param suffix string that is added at the end of JAR name
   */
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
            logger.error("Error while copying OpenTelemetry file " + entryName + "to main JAR.");
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

  /**
   * Copies the entry with prefix specific for framework.
   *
   * @param entry JarEntry that is copied
   * @param zout ZipOutputStream the entry is copied to
   * @throws IOException If process of creating entry from file throws I/O exception.
   * @see agh.edu.pl.utils.ZipEntryCreator#createZipEntryFromFile(ZipOutputStream, File, String)
   */
  private void copySingleEntryWithFrameworkSupport(JarEntry entry, ZipOutputStream zout)
      throws IOException {
    File tmpFile =
        copySingleEntryFromJar(entry, agentJar, folderNames.getOpenTelemetryClassesPackage());
    String prefix = frameworkSupport.getClassesPrefix();
    String newEntryPath = prefix + entry.getName();
    createZipEntryFromFile(zout, tmpFile, newEntryPath);
  }

  /**
   * Copies the entry from '/inst' folder outside of this folder with prefix specific for framework.
   *
   * @param entry JarEntry that is copied
   * @param zout ZipOutputStream the entry is copied to
   * @throws IOException If process of creating entry from file throws I/O exception.
   * @see agh.edu.pl.utils.ZipEntryCreator#createZipEntryFromFile(ZipOutputStream, File, String)
   */
  private void copySingleInstFolderFile(JarEntry entry, ZipOutputStream zout) throws IOException {
    File tmpFile =
        copySingleEntryFromJar(entry, agentJar, folderNames.getOpenTelemetryClassesPackage());
    String prefix = frameworkSupport != null ? frameworkSupport.getClassesPrefix() : "";
    String newEntryPath = entry.getName().replace("inst/", prefix);
    createZipEntryFromFile(zout, tmpFile, newEntryPath);
  }

  /**
   * Copies the main project class with prefix specific for framework.
   *
   * @param entry JarEntry that is copied
   * @param zout ZipOutputStream the entry is copied to
   * @param jarFile file that contains the entry
   * @param frameworkSupport FrameworkSupport
   * @throws IOException
   * @see FrameworkSupport
   * @see agh.edu.pl.utils.ZipEntryCreator#createZipEntryFromFile(ZipOutputStream, File, String)
   * @see agh.edu.pl.utils.ZipEntryCreator#copySingleEntryFromJar(JarEntry, JarFile, String)
   */
  private void copyMainClassWithPrefix(
      JarEntry entry, ZipOutputStream zout, JarFile jarFile, FrameworkSupport frameworkSupport)
      throws IOException {
    File tmpFile =
        copySingleEntryFromJar(entry, jarFile, folderNames.getOpenTelemetryClassesPackage());
    String newEntryPath = frameworkSupport.getClassesPrefix() + entry.getName();
    createZipEntryFromFile(zout, tmpFile, newEntryPath);
  }

  /**
   * Copies the entry with 'classdata' file extension with '.class' file extension and
   * with prefix specific for framework.
   *
   * @param entry JarEntry that is copied
   * @param zout ZipOutputStream the entry is copied to
   * @throws IOException If process of creating entry from file throws I/O exception.
   * @see agh.edu.pl.utils.ZipEntryCreator#createZipEntryFromFile(ZipOutputStream, File, String)
   */
  private void copySingleClassdataFile(JarEntry entry, ZipOutputStream zout) throws IOException {
    File tmpFile =
        copySingleEntryFromJar(entry, agentJar, folderNames.getOpenTelemetryClassesPackage());
    String prefix = frameworkSupport != null ? frameworkSupport.getClassesPrefix() : "";
    String newEntryPath = entry.getName().replace(".classdata", ".class");
    newEntryPath = newEntryPath.replace("inst/", prefix);

    createZipEntryFromFile(zout, tmpFile, newEntryPath);
  }
}
