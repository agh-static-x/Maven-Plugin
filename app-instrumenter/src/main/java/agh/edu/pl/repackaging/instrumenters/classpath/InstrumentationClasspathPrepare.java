package agh.edu.pl.repackaging.instrumenters.classpath;

import agh.edu.pl.repackaging.config.InstrumentationConfiguration;
import agh.edu.pl.repackaging.config.TemporaryFolders;
import agh.edu.pl.repackaging.frameworks.FrameworkSupport;
import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.maven.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Prepares tthe configuration for instrumentation process. */
public class InstrumentationClasspathPrepare {
  private final File mainFile;
  private final TemporaryFolders temporaryFolders = TemporaryFolders.getInstance();
  private final FrameworkSupport frameworkSupport;
  private final HashMap<Artifact, Boolean> artifactMap;
  private final Logger logger = LoggerFactory.getLogger(InstrumentationClasspathPrepare.class);

  public InstrumentationClasspathPrepare(
      File mainFile, FrameworkSupport frameworkSupport, HashMap<Artifact, Boolean> artifactMap) {
    this.mainFile = mainFile;
    this.frameworkSupport = frameworkSupport;
    this.artifactMap = artifactMap;
  }

  /**
   * Prepares the <code>classpath</code>, unpacks the dependencies from the main file to temporary
   * folder (only if they are in JAR format) and checks which of the dependencies are transitive
   * (those dependencies are not included in the output JAR file, but need to be put on the <code>
   * classpath
   * </code> and put into temporary folder to improve the instrumentation process). If the main JAR
   * file could not be copy, the problem is logged. If the main file could not be parsed as JAR
   * file, the problem is logged. If the ZipOutputStream creation from file fails, the problem is
   * logged. If the dependency could not be copied to temporary folder, the problem is logged.
   *
   * @return configuration for instrumentation process
   * @see ZipOutputStream
   * @see JarEntry
   */
  public InstrumentationConfiguration prepareClasspath() {
    InstrumentationConfiguration instrumentationConfiguration = new InstrumentationConfiguration();

    StringBuilder classpath = new StringBuilder();

    String fileName =
        String.format("%s/%s", temporaryFolders.getMainJARInitialCopyPackage(), mainFile.getName());
    File f = new File(fileName);
    if (!f.getParentFile().mkdirs() && !f.getParentFile().exists()) {
      logger.error(
          "The temporary directory "
              + f.getPath()
              + " necessary for main file instrumentation process could not be created. Please make sure you have permissions required to create a directory.");
      return null;
    }
    JarFile mainJar;
    try {
      mainJar = new JarFile(this.mainFile);
    } catch (IOException e) {
      logger.error(
          "Problem occurred while getting project JAR file "
              + this.mainFile.getName()
              + ". Make sure you have defined JAR packaging in pom.xml.");
      return null;
    }
    ZipOutputStream zout;
    try {
      zout = new ZipOutputStream(new FileOutputStream(f));
    } catch (FileNotFoundException exception) {
      logger.error(
          "Could not create output stream for JAR file, because file "
              + f.getPath()
              + " does not exist.");
      return null;
    }
    zout.setMethod(ZipOutputStream.STORED);
    for (Enumeration<JarEntry> enums = mainJar.entries(); enums.hasMoreElements(); ) {
      JarEntry entry = enums.nextElement();
      try {
        storeSingleJAREntry(entry, mainJar, zout);
      } catch (IOException exception) {
        logger.error("Error occurred while adding dependency " + entry.getName() + " to main JAR.");
        return null;
      }
    }
    try {
      zout.close();
      mainJar.close();
    } catch (IOException exception) {
      logger.error("JAR file/output stream was not closed properly.");
    }
    classpath.append(fileName).append(File.pathSeparator);

    JarFile jarFile;
    try {
      jarFile = new JarFile(this.mainFile);
    } catch (IOException e) {
      logger.error(
          "Problem occurred while getting project JAR file "
              + this.mainFile.getName()
              + ". Make sure you have defined JAR packaging in pom.xml.");
      instrumentationConfiguration.setClasspath(classpath.toString());
      return instrumentationConfiguration;
    }

    for (Enumeration<JarEntry> enums = jarFile.entries(); enums.hasMoreElements(); ) {
      JarEntry entry = enums.nextElement();
      if (entry.getName().endsWith(".jar")) {
        unpackSingleDependency(entry, jarFile, classpath);
      }
    }

    StringBuilder transitiveDependencies = new StringBuilder();

    for (Map.Entry<Artifact, Boolean> entry : artifactMap.entrySet()) {
      if (entry.getValue()) {
        Artifact artifact = entry.getKey();
        File file = artifact.getFile();
        String outFileName =
            String.format("%s/%s", temporaryFolders.getMainJARInitialCopyPackage(), file.getName());
        File outFile = new File(outFileName);
        try {
          Files.copy(file.toPath(), outFile.toPath());
        } catch (IOException exception) {
          logger.error(
              "Couldn't copy transitive dependency " + file.getPath() + " to temporary directory.");
        }
        classpath.append(outFileName).append(File.pathSeparator);
        transitiveDependencies.append(outFileName).append(File.pathSeparator);
      }
    }
    instrumentationConfiguration.setTransitiveDependencies(transitiveDependencies.toString());
    instrumentationConfiguration.setClasspath(classpath.toString());
    return instrumentationConfiguration;
  }

  /**
   * Unpacks entry from file to the temporary folder, marks the dependency as non-transitive and
   * adds the path to temporary file with unpacked entry to the classpath. If the temporary
   * directories to store the file could not be created, the problem is logged. If the file could
   * not be copied to temporary folder, the problem is logged.
   *
   * @param entry JarEntry that is being unpacked to temporary folder
   * @param jarFile file that contains the entry
   * @param classpath classpath that path to unpacked entry is to be put on
   * @see InputStream
   * @see Artifact
   */
  private void unpackSingleDependency(JarEntry entry, JarFile jarFile, StringBuilder classpath) {
    String fileName =
        String.format("%s/%s", temporaryFolders.getMainJARInitialCopyPackage(), entry.getName());
    File f = new File(fileName);
    if (!f.getParentFile().mkdirs() && !f.getParentFile().exists()) {
      logger.error(
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
      logger.error("Could not copy JAR dependency " + entry.getName() + " to temporary folder.");
      return;
    }
    for (Artifact artifact : artifactMap.keySet()) {
      if (artifact.getFile().getName().equals(f.getName())) {
        artifactMap.put(artifact, false);
      }
    }
    classpath.append(fileName).append(File.pathSeparator);
  }

  /**
   * Transfers JAR entry from one file to other file. If the file is build based on the supported
   * framework, it is stored in output file without prefix 9to avoid problems with shading during
   * instrumentation process).
   *
   * @param entry JarEntry that is being transferred to other file
   * @param inputFile JAR that contains the entry
   * @param zout ZipOutputStream of the file entry should be transferred to
   * @throws IOException If there will be any I/O exception during the transfer process or during
   *     the file closing process
   * @see InputStream
   * @see ZipEntry
   * @see JarEntry
   */
  private void storeSingleJAREntry(JarEntry entry, JarFile inputFile, ZipOutputStream zout)
      throws IOException {
    if (frameworkSupport != null
        && entry.getName().startsWith(frameworkSupport.getClassesPrefix())
        && !entry.isDirectory()) {
      frameworkSupport.copyMainClassWithoutPrefix(entry, zout, inputFile);
    } else {
      ZipEntry outEntry = new ZipEntry(entry);
      zout.putNextEntry(outEntry);
      InputStream in = inputFile.getInputStream(entry);
      in.transferTo(zout);
      in.close();
      zout.closeEntry();
    }
  }
}
