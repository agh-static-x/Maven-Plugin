/* (C)2021 */
package agh.edu.pl.repackaging.instrumenters.mainclass;

import agh.edu.pl.repackaging.config.FolderNames;
import agh.edu.pl.repackaging.config.InstrumentationConstants;
import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import org.codehaus.plexus.util.FileUtils;

public class MainJarInstrumenter {

  private final File file;
  private final String agentPath;
  private final FolderNames folderNames = FolderNames.getInstance();

  public MainJarInstrumenter(File file, String agentPath) {
    this.file = file;
    this.agentPath = agentPath;
  }

  public void instrumentMain() {
    try {

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
      final String outFileName =
          folderNames.getJARWithInstrumentedDependenciesPackage()
              + File.separator
              + outFileNameParts[outFileNameParts.length - 1];
      String mainPath = outFileName + File.pathSeparator;
      Process process;
      try {
        process =
            InstrumentationConstants.getInstrumentationProcess(
                    agentPath, mainPath, folderNames.getInstrumentedJARPackage())
                .inheritIO()
                .start();
      } catch (IOException exception) {
        System.err.println("Error occurred during the instrumentation process for main JAR.");
        return;
      }
      try {
        int ret = process.waitFor();
        if (ret != 0) {
          System.err.println(
              "The instrumentation process for main JAR finished with exit value " + ret + ".");
        }
      } catch (InterruptedException exception) {
        System.err.println("The instrumentation process for main JAR was interrupted.");
      }
    } finally {
      try {
        FileUtils.deleteDirectory(folderNames.getJARWithInstrumentedDependenciesPackage());
      } catch (IOException exception) {
        System.err.println(
            "Temporary directory required for main JAR instrumentation process was not deleted properly.");
      }
    }
  }
}
