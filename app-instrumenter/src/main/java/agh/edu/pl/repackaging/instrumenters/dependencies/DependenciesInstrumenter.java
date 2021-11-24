/* (C)2021 */
package agh.edu.pl.repackaging.instrumenters.dependencies;

import static agh.edu.pl.utils.ZipEntryCreator.createZipEntryFromFile;

import agh.edu.pl.repackaging.config.FolderNames;
import agh.edu.pl.repackaging.config.InstrumentationConstants;
import agh.edu.pl.repackaging.frameworks.FrameworkSupport;
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
  private final FrameworkSupport frameworkSupport;

  public DependenciesInstrumenter(File file, String agentPath, FrameworkSupport frameworkSupport) {
    this.file = file;
    this.agentPath = agentPath;
    this.frameworkSupport = frameworkSupport;
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

      String fileSeparatorPattern = Pattern.quote(System.getProperty("file.separator"));
      String[] outFileNameParts = jarFile.getName().split(fileSeparatorPattern);
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
      StringBuilder classpath = new StringBuilder();

      for (Enumeration<JarEntry> enums = jarFile.entries(); enums.hasMoreElements(); ) {
        JarEntry entry = enums.nextElement();
        if (entry.getName().endsWith(".jar")) {
          unpackSingleDependency(entry, jarFile, classpath);
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
      Process process = null;
      try {
        process =
            InstrumentationConstants.getInstrumentationProcess(
                    agentPath, classpath.toString(), folderNames.getInstrumentedDependencyPackage())
                .inheritIO()
                .start();
      } catch (IOException exception) {
        System.err.println(
            "Error occurred during the instrumentation process for JAR dependencies.");
      }
      int ret;
      if (process != null) {
        try {
          ret = process.waitFor();
          if (ret != 0) {
            System.err.println(
                "The instrumentation process for JAR dependencies finished with exit value "
                    + ret
                    + ".");
          }
        } catch (InterruptedException exception) {
          System.err.println("The instrumentation process for JAR dependencies was interrupted.");
        }
      }
      String[] dependenciesPathsList = classpath.toString().split(File.pathSeparator);
      for (String dependencyPath : dependenciesPathsList) {
        String[] dependencyPathElements = dependencyPath.split("/");
        String dependencyName = dependencyPathElements[dependencyPathElements.length - 1];
        try {
          createZipEntryFromFile(
              zout,
              new File(
                  String.format(
                      "%s/%s", folderNames.getInstrumentedDependencyPackage(), dependencyName)),
              dependencyPath.replaceFirst(folderNames.getMainJARInitialCopyPackage() + "/", ""));
        } catch (IOException exception) {
          System.err.println(
              "Exception occurred while adding instrumented dependency "
                  + dependencyName
                  + " to main JAR.");
          exception.printStackTrace();
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
    if (frameworkSupport != null
        && entry.getName().startsWith(frameworkSupport.getClassesPrefix())
        && !entry.isDirectory()) {
      frameworkSupport.copyMainClassWithoutPrefix(entry, zout, jarFile);
    } else {
      ZipEntry outEntry = new ZipEntry(entry);
      zout.putNextEntry(outEntry);
      InputStream in = jarFile.getInputStream(entry);
      in.transferTo(zout);
      in.close();
      zout.closeEntry();
    }
  }

  private void unpackSingleDependency(JarEntry entry, JarFile jarFile, StringBuilder classpath) {
    String fileName =
        String.format("%s/%s", folderNames.getMainJARInitialCopyPackage(), entry.getName());
    File f = new File(fileName);
    if (!f.getParentFile().mkdirs() && !f.getParentFile().exists()) {
      System.err.println(
          "The temporary directory necessary for dependency "
              + entry.getName()
              + " instrumentation process could not be created. Please make sure you have permissions required to create a directory.");
      return;
    }
    try {
      InputStream jarFileInputStream = jarFile.getInputStream(entry);
      Files.copy(jarFileInputStream, f.toPath());
      jarFileInputStream.close();
    } catch (IOException exception) {
      System.err.println(
          "Could not copy JAR dependency " + entry.getName() + " to temporary folder.");
      return;
    }
    classpath.append(fileName).append(File.pathSeparator);
  }
}
