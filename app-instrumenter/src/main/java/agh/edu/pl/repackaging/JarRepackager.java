/* (C)2021 */
package agh.edu.pl.repackaging;

import agh.edu.pl.repackaging.classes.AgentClassesExtractor;
import agh.edu.pl.repackaging.config.FolderNames;
import agh.edu.pl.repackaging.config.InstrumentationConstants;
import agh.edu.pl.repackaging.frameworks.AppFramework;
import agh.edu.pl.repackaging.frameworks.FrameworkSupport;
import agh.edu.pl.repackaging.instrumenters.classpath.InstrumentationClasspathPrepare;
import agh.edu.pl.repackaging.instrumenters.instrumentation.JarWithDependenciesInstrumenter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

public class JarRepackager {
  private String agentPath;
  private File jarFile;
  private final FolderNames folderNames = FolderNames.getInstance();
  private FrameworkSupport frameworkSupport;

  public JarRepackager() {
    this.copyInstrumentedOtelJar();
  }

  public void copyInstrumentedOtelJar() {
    this.agentPath =
        folderNames.getInstrumentedOtelJarPackage()
            + File.separator
            + InstrumentationConstants.OTEL_AGENT_JAR_FILENAME;

    Path path = Paths.get(this.agentPath);
    try {
      Files.createDirectories(path.getParent());
    } catch (IOException exception) {
      System.err.println(
          "Error when creating temporary directories for agent JAR. Please make sure you have permissions required to create a directory.");
    }

    try {
      Files.copy(
          JarRepackager.class
              .getClassLoader()
              .getResourceAsStream(InstrumentationConstants.OTEL_AGENT_JAR_FILENAME),
          path,
          StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException exception) {
      System.err.println("Couldn't copy agent JAR from plugin resources.");
    }
  }

  public void setJarFile(File jarFile) {
    this.jarFile = jarFile;
  }

  public void repackageJar() {
    String classpath =
        new InstrumentationClasspathPrepare(jarFile, frameworkSupport).prepareClasspath();
    String[] nameParts = jarFile.getName().split("/");
    String mainFileName = nameParts[nameParts.length - 1];
    new JarWithDependenciesInstrumenter(classpath, agentPath, mainFileName)
        .instrumentJarWithDependencies();
  }

  public void addOpenTelemetryClasses(String suffix) {
    String pattern = Pattern.quote(System.getProperty("file.separator"));
    String[] outFileNameParts = jarFile.getName().split(pattern);
    AgentClassesExtractor agentClassesExtractor =
        new AgentClassesExtractor(
            new File(
                folderNames.getInstrumentedJARPackage(),
                outFileNameParts[outFileNameParts.length - 1]),
            agentPath,
            folderNames.getInstrumentedJARPackage());
    agentClassesExtractor.addOpenTelemetryFolders(frameworkSupport, suffix);
  }

  public void checkFrameworkSupport() {
    this.frameworkSupport = new AppFramework().getAppFramework(jarFile);
  }
}
