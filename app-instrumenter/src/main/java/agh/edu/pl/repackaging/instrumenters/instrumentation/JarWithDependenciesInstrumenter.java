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

public class JarWithDependenciesInstrumenter {
  private final InstrumentationConfiguration instrumentationConfiguration;
  private final String agentPath;
  private final FolderNames folderNames = FolderNames.getInstance();
  private final String mainFileName;

  public JarWithDependenciesInstrumenter(
      InstrumentationConfiguration instrumentationConfiguration,
      String agentPath,
      String mainFileName) {
    this.instrumentationConfiguration = instrumentationConfiguration;
    this.agentPath = agentPath;
    this.mainFileName = mainFileName;
  }

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
      File instrumentedMainFile =
          new File(folderNames.getJARWithInstrumentedDependenciesPackage(), mainFileName);
      JarFile instrumentedMainJar;
      try {
        instrumentedMainJar = new JarFile(instrumentedMainFile);
      } catch (IOException e) {
        System.err.println(
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
        System.err.println(
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
            System.err.println(
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
            System.err.println(
                "Error occurred while adding dependency " + entry.getName() + " to main JAR.");
            return;
          }
        }
      }
      try {
        zout.close();
        instrumentedMainJar.close();
      } catch (IOException exception) {
        System.err.println("JAR file/output stream was not closed properly.");
      }
    } finally {
      try {
        FileUtils.deleteDirectory(folderNames.getMainJARInitialCopyPackage());
        FileUtils.deleteDirectory(folderNames.getJARWithInstrumentedDependenciesPackage());
      } catch (IOException exception) {
        System.err.println(
            "Temporary directories required for dependencies instrumentation process were not deleted properly.");
      }
    }
  }
}
