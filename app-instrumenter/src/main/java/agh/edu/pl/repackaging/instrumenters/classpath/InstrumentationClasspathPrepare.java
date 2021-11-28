package agh.edu.pl.repackaging.instrumenters.classpath;

import agh.edu.pl.repackaging.config.FolderNames;
import agh.edu.pl.repackaging.frameworks.FrameworkSupport;
import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class InstrumentationClasspathPrepare {
  private final File mainFile;
  private final FolderNames folderNames = FolderNames.getInstance();
  private final FrameworkSupport frameworkSupport;

  public InstrumentationClasspathPrepare(File mainFile, FrameworkSupport frameworkSupport) {
    this.mainFile = mainFile;
    this.frameworkSupport = frameworkSupport;
  }

  public String prepareClasspath() {
    createInitialFolders();

    StringBuilder classpath = new StringBuilder();

    String fileName =
        String.format("%s/%s", folderNames.getMainJARInitialCopyPackage(), mainFile.getName());
    File f = new File(fileName);
    if (!f.getParentFile().mkdirs() && !f.getParentFile().exists()) {
      System.err.println(
          "The temporary directory "
              + f.getPath()
              + " necessary for main file instrumentation process could not be created. Please make sure you have permissions required to create a directory.");
      return "";
    }
    JarFile mainJar;
    try {
      mainJar = new JarFile(this.mainFile);
    } catch (IOException e) {
      System.err.println(
          "Problem occurred while getting project JAR file "
              + this.mainFile.getName()
              + ". Make sure you have defined JAR packaging in pom.xml.");
      return "";
    }
    ZipOutputStream zout;
    try {
      zout = new ZipOutputStream(new FileOutputStream(f));
    } catch (FileNotFoundException exception) {
      System.err.println(
          "Could not create output stream for JAR file, because file "
              + f.getPath()
              + " does not exist.");
      return "";
    }
    zout.setMethod(ZipOutputStream.STORED);
    for (Enumeration<JarEntry> enums = mainJar.entries(); enums.hasMoreElements(); ) {
      JarEntry entry = enums.nextElement();
      try {
        storeSingleJAREntry(entry, mainJar, zout);
      } catch (IOException exception) {
        System.err.println(
            "Error occurred while adding dependency " + entry.getName() + " to main JAR.");
        return "";
      }
    }
    try {
      zout.close();
      mainJar.close();
    } catch (IOException exception) {
      System.err.println("JAR file/output stream was not closed properly.");
    }
    classpath.append(fileName).append(File.pathSeparator);

    JarFile jarFile;
    try {
      jarFile = new JarFile(this.mainFile);
    } catch (IOException e) {
      System.err.println(
          "Problem occurred while getting project JAR file "
              + this.mainFile.getName()
              + ". Make sure you have defined JAR packaging in pom.xml.");
      return classpath.toString();
    }

    for (Enumeration<JarEntry> enums = jarFile.entries(); enums.hasMoreElements(); ) {
      JarEntry entry = enums.nextElement();
      if (entry.getName().endsWith(".jar")) {
        unpackSingleDependency(entry, jarFile, classpath);
      }
    }
    return classpath.toString();
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

  private void createInitialFolders() {
    File tmpDir = new File(folderNames.getMainJARInitialCopyPackage());
    File instrumentedDir = new File(folderNames.getJARWithInstrumentedDependenciesPackage());
    File mainInstrumentedDir = new File(folderNames.getInstrumentedJARPackage());
    if (!tmpDir.mkdir() || !instrumentedDir.mkdir() || mainInstrumentedDir.mkdir()) {
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
}
