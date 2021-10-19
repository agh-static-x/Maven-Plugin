/* (C)2021 */
package agh.edu.pl.repackaging;

import agh.edu.pl.repackaging.classes.OpenTelemetryClasses;
import agh.edu.pl.repackaging.config.FolderNames;
import agh.edu.pl.repackaging.instrumenters.dependencies.DependenciesInstrumenter;
import agh.edu.pl.repackaging.instrumenters.mainclass.MainJarInstrumenter;
import java.io.*;
import java.util.regex.Pattern;

public class JarRepackager {
  private final String agentPath;
  private File jarFile;

  public JarRepackager(String agentPath) {
    this.agentPath = FolderNames.INSTRUMENTED_OTEL_JAR + "/" + agentPath;
  }

  public void setJarFile(File jarFile) {
    this.jarFile = jarFile;
  }

  public void repackageJar() throws Exception {
    new DependenciesInstrumenter(jarFile, agentPath).instrumentDependencies();
    new MainJarInstrumenter(jarFile, agentPath).instrumentMain();
  }

  public void addOpenTelemetryClasses() {
    String pattern = Pattern.quote(System.getProperty("file.separator"));
    String[] outFileNameParts = jarFile.getName().split(pattern);
    final String outFileName =
        FolderNames.INSTRUMENTED_JAR + "/" + outFileNameParts[outFileNameParts.length - 1];
    OpenTelemetryClasses openTelemetryClasses =
        new OpenTelemetryClasses(new File(outFileName), agentPath, FolderNames.INSTRUMENTED_JAR);
    try {
      openTelemetryClasses.addOpenTelemetryFolders();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
