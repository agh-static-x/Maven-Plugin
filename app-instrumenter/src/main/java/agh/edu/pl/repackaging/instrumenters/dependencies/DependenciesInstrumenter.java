/* (C)2021 */
package agh.edu.pl.repackaging.instrumenters.dependencies;

import static agh.edu.pl.utils.ZipEntryCreator.createZipEntryFromFile;

import agh.edu.pl.repackaging.config.FolderNames;
import agh.edu.pl.repackaging.config.InstrumentationConstants;
import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.codehaus.plexus.util.FileUtils;

public class DependenciesInstrumenter {

  private final File file;
  private final String agentPath;
  private final FolderNames folderNames = FolderNames.getInstance();

  public DependenciesInstrumenter(File file, String agentPath) {
    this.file = file;
    this.agentPath = agentPath;
  }

  public void instrumentDependencies() {
    try {
      createInitialFolders();

      JarFile jarFile;
      try {
        jarFile = new JarFile(this.file);
      } catch (IOException e) {
        System.err.println(
            "Problem occurred while getting project JAR file. Make sure you have defined JAR packaging in pom.xml.");
        return;
      }

      String pattern = Pattern.quote(System.getProperty("file.separator"));
      String[] outFileNameParts = jarFile.getName().split(pattern);
      final String outFileName = outFileNameParts[outFileNameParts.length - 1];
      final File outFile =
          new File(folderNames.getJARWithInstrumentedDependenciesPackage(), outFileName);
      ZipOutputStream zout;
      try {
        zout = new ZipOutputStream(new FileOutputStream(outFile));
      } catch (FileNotFoundException exception) {
        System.err.println(
            "Could not create output stream for JAR file, because file does not exist.");
        return;
      }
      zout.setMethod(ZipOutputStream.STORED);

      for (Enumeration<JarEntry> enums = jarFile.entries(); enums.hasMoreElements(); ) {
        JarEntry entry = enums.nextElement();
        if (entry.getName().endsWith(".jar")) {
          instrumentSingleDependency(entry, jarFile, zout);
        } else {
          try {
            storeSingleJAREntry(entry, jarFile, zout);
          } catch (IOException exception) {
            System.err.println(
                "Error occurred while adding dependency " + entry.getName() + " to main JAR.");
            return;
          }
        }
      }
      try {
        zout.close();
        jarFile.close();
      } catch (IOException exception) {
        System.err.println("JAR file/output stream was not closed properly.");
      }
    } finally {
      try {
        FileUtils.deleteDirectory(folderNames.getMainJARInitialCopyPackage());
        FileUtils.deleteDirectory(folderNames.getInstrumentedDependencyPackage());
      } catch (IOException exception) {
        System.err.println(
            "Temporary directories required for dependencies instrumentation process were not deleted properly.");
      }
    }
  }

  private void createInitialFolders() {
    File tmpDir = new File(folderNames.getMainJARInitialCopyPackage());
    File instrumentedDir = new File(folderNames.getJARWithInstrumentedDependenciesPackage());
    File instrumentedFileDirectory = new File(folderNames.getInstrumentedDependencyPackage());
    if (!tmpDir.mkdir() || !instrumentedDir.mkdir() || !instrumentedFileDirectory.mkdir()) {
      System.err.println(
          "The temporary directories necessary for JAR instrumentation process could not be created. Please make sure you have permissions required to create a directory.");
    }
  }

  private void storeSingleJAREntry(JarEntry entry, JarFile jarFile, ZipOutputStream zout)
      throws IOException {
    ZipEntry outEntry = new ZipEntry(entry);
    zout.putNextEntry(outEntry);
    InputStream in = jarFile.getInputStream(entry);
    in.transferTo(zout);
    in.close();
    zout.closeEntry();
  }

  private void instrumentSingleDependency(JarEntry entry, JarFile jarFile, ZipOutputStream zout) {
    StringBuilder classpath = new StringBuilder();
    String fileName =
        String.format("%s/%s", folderNames.getMainJARInitialCopyPackage(), entry.getName());
    File f = new File(fileName);
    if (!f.getParentFile().mkdirs()) {
      System.err.println(
          "The temporary directory necessary for dependency "
              + entry.getName()
              + " instrumentation process could not be created. Please make sure you have permissions required to create a directory.");
      return;
    }
    try {
      Files.copy(jarFile.getInputStream(entry), f.toPath());
    } catch (IOException exception) {
      System.err.println(
          "Could not copy JAR dependency " + entry.getName() + " to temporary folder.");
      return;
    }
    classpath.append(fileName).append(File.pathSeparator);
    System.out.println(classpath);
    Process process;
    try {
      process =
          InstrumentationConstants.getInstrumentationProcess(
                  agentPath, classpath.toString(), folderNames.getInstrumentedDependencyPackage())
              .inheritIO()
              .start();
    } catch (IOException exception) {
      System.err.println(
          "Error occurred during the instrumentation process for JAR dependency "
              + entry.getName()
              + ".");
      return;
    }
    try {
      int ret = process.waitFor();
      if (ret != 0) {
        System.err.println(
            "The instrumentation process for JAR dependency "
                + entry.getName()
                + " finished with exit value "
                + ret
                + ".");
        return;
      }
    } catch (InterruptedException exception) {
      System.err.println(
          "The instrumentation process for JAR dependency "
              + entry.getName()
              + " was interrupted.");
      return;
    }
    String[] fileNameParts = entry.getName().split("/");
    try {
      createZipEntryFromFile(
          zout,
          new File(
              folderNames.getInstrumentedDependencyPackage()
                  + File.separator
                  + fileNameParts[fileNameParts.length - 1]),
          entry.getName());
    } catch (IOException exception) {
      System.err.println(
          "Exception occurred while adding instrumented dependency "
              + entry.getName()
              + " to main JAR.");
      return;
    }
    try {
      FileUtils.cleanDirectory(folderNames.getMainJARInitialCopyPackage());
      FileUtils.cleanDirectory(folderNames.getInstrumentedDependencyPackage());
    } catch (IOException exception) {
      System.err.println(
          "Error while cleaning temporary directories after dependency "
              + entry.getName()
              + " was instrumented.");
    }
  }
}
