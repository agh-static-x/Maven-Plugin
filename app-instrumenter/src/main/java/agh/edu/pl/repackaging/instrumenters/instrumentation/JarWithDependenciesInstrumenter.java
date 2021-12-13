package agh.edu.pl.repackaging.instrumenters.instrumentation;

import static agh.edu.pl.utils.ZipEntryCreator.createZipEntryFromFile;

import agh.edu.pl.repackaging.config.FolderNames;
import agh.edu.pl.repackaging.config.InstrumentationConfiguration;
import agh.edu.pl.repackaging.config.InstrumentationConstants;
import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Contains methods that supports the process of file (with its dependencies) instrumentation. */
public class JarWithDependenciesInstrumenter {
  private final InstrumentationConfiguration instrumentationConfiguration;
  private final String agentPath;
  private final FolderNames folderNames = FolderNames.getInstance();
  private final String mainFileName;
  private final Logger logger = LoggerFactory.getLogger(JarWithDependenciesInstrumenter.class);

  public JarWithDependenciesInstrumenter(
      InstrumentationConfiguration instrumentationConfiguration,
      String agentPath,
      String mainFileName) {
    this.instrumentationConfiguration = instrumentationConfiguration;
    this.agentPath = agentPath;
    this.mainFileName = mainFileName;
  }

  /**
   * Instruments JAR (WAR) and its dependencies (if they are embedded JARs). Creates the
   * instrumentation process based on provided configuration and includes the transitive
   * dependencies in it. Starts the instrumentation process and waits for its end. Packages the
   * instrumented JAR dependencies and puts them as entries into the main JAR file. Deletes the
   * temporary folders that are no longer needed after this stage.
   * If any exception occurred during the execution of the java process, the error is logged.
   * If the process exits with non-zero value, it is logged.
   * If the process is interrupted, the error is logged.
   * If problem occurred while getting instrumented file, the error is logged.
   * If there is a problem with creating the JAR entry from file, the error is logged.
   * If temporary folders required for the process are not deleted, the error is logged.
   *
   * @see Process
   * @see ZipOutputStream
   * @see JarFile
   * @see JarEntry
   */
  public void instrumentJarWithDependencies() {
    try {
      Process process = null;
      try {
        process =
            InstrumentationConstants.getInstrumentationProcess(
                    agentPath,
                    instrumentationConfiguration.getClasspath(),
                    folderNames.getJARWithInstrumentedDependenciesPackage(),
                    instrumentationConfiguration.getTransitiveDependencies())
                .inheritIO()
                .start();
      } catch (IOException exception) {
        logger.error("Error occurred during the instrumentation process for JAR dependencies.");
      }
      int ret;
      if (process != null) {
        try {
          ret = process.waitFor();
          if (ret != 0) {
            logger.error(
                "The instrumentation process for JAR dependencies finished with exit value "
                    + ret
                    + ".");
          }
        } catch (InterruptedException exception) {
          logger.error("The instrumentation process for JAR dependencies was interrupted.");
        }
      }
      File instrumentedMainFile =
          new File(folderNames.getJARWithInstrumentedDependenciesPackage(), mainFileName);
      JarFile instrumentedMainJar;
      try {
        instrumentedMainJar = new JarFile(instrumentedMainFile);
      } catch (IOException e) {
        logger.error(
            "Problem occurred while getting project JAR file "
                + instrumentedMainFile.getName()
                + ". Make sure you have defined JAR packaging in pom.xml.");
        return;
      }
      final File outFile = new File(folderNames.getInstrumentedJARPackage(), mainFileName);
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

      for (Enumeration<JarEntry> enums = instrumentedMainJar.entries(); enums.hasMoreElements(); ) {
        JarEntry entry = enums.nextElement();
        if (entry.getName().endsWith(".jar")) {
          String[] dependencyPathElements = entry.getName().split("/");
          String dependencyName = dependencyPathElements[dependencyPathElements.length - 1];
          try {
            createZipEntryFromFile(
                zout,
                new File(
                    String.format(
                        "%s/%s",
                        folderNames.getJARWithInstrumentedDependenciesPackage(), dependencyName)),
                entry.getName());
          } catch (IOException exception) {
            logger.error(
                "Exception occurred while adding instrumented dependency "
                    + dependencyName
                    + " to main JAR.");
            exception.printStackTrace();
          }
        } else {
          try {
            ZipEntry outEntry = new ZipEntry(entry);
            zout.putNextEntry(outEntry);
            InputStream in = instrumentedMainJar.getInputStream(entry);
            in.transferTo(zout);
            in.close();
            zout.closeEntry();
          } catch (IOException exception) {
            logger.error(
                "Error occurred while adding dependency " + entry.getName() + " to main JAR.");
            return;
          }
        }
      }
      try {
        zout.close();
        instrumentedMainJar.close();
      } catch (IOException exception) {
        logger.error("JAR file/output stream was not closed properly.");
      }
    } finally {
      try {
        FileUtils.deleteDirectory(folderNames.getMainJARInitialCopyPackage());
        FileUtils.deleteDirectory(folderNames.getJARWithInstrumentedDependenciesPackage());
      } catch (IOException exception) {
        logger.error(
            "Temporary directories required for dependencies instrumentation process were not deleted properly.");
      }
    }
  }
}
