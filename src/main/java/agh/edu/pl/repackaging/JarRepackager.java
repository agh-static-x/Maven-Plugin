/* (C)2021 */
package agh.edu.pl.repackaging;

import agh.edu.pl.repackaging.classes.AgentClassesExtractor;
import agh.edu.pl.repackaging.config.FolderNames;
import agh.edu.pl.repackaging.instrumenters.dependencies.DependenciesInstrumenter;
import agh.edu.pl.repackaging.instrumenters.mainclass.MainJarInstrumenter;
import java.io.*;
import java.util.regex.Pattern;

public class JarRepackager {
  private final String agentPath;
  private File jarFile;
  private final FolderNames folderNames = FolderNames.getInstance();

  public JarRepackager(String agentPath) {
    this.agentPath = folderNames.getInstrumentedOtelJarPackage() + File.separator + agentPath;
  }

  public void setJarFile(File jarFile) {
    this.jarFile = jarFile;
  }

  public void repackageJar() {
    new DependenciesInstrumenter(jarFile, agentPath).instrumentDependencies();
    new MainJarInstrumenter(jarFile, agentPath).instrumentMain();
  }

  public void addOpenTelemetryClasses() {
    String pattern = Pattern.quote(System.getProperty("file.separator"));
    String[] outFileNameParts = jarFile.getName().split(pattern);
    final String outFileName =
        folderNames.getInstrumentedJARPackage()
            + File.separator
            + outFileNameParts[outFileNameParts.length - 1];
    AgentClassesExtractor agentClassesExtractor =
        new AgentClassesExtractor(
            new File(outFileName), agentPath, folderNames.getInstrumentedJARPackage());
    agentClassesExtractor.addOpenTelemetryFolders();
  }
}
