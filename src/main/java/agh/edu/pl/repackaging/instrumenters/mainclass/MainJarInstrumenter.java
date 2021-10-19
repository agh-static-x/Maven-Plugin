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

  public MainJarInstrumenter(File file, String agentPath) {
    this.file = file;
    this.agentPath = agentPath;
  }

  public void instrumentMain() throws Exception {
    try {
      JarFile jarFile = null;
      try {
        jarFile = new JarFile(this.file);
      } catch (IOException e) {
        e.printStackTrace();
      }
      String pattern = Pattern.quote(System.getProperty("file.separator"));
      if (jarFile == null) {
        System.err.println("No JAR for project found.");
        return;
      }
      String[] outFileNameParts = jarFile.getName().split(pattern);
      final String outFileName =
          FolderNames.JAR_WITH_INSTRUMENTED_DEPENDENCIES
              + "/"
              + outFileNameParts[outFileNameParts.length - 1];
      String mainPath = outFileName + File.pathSeparator;
      Process process =
          InstrumentationConstants.getInstrumentationProcess(
                  agentPath, mainPath, FolderNames.INSTRUMENTED_JAR)
              .inheritIO()
              .start();
      int ret = process.waitFor();
    } finally {
      FileUtils.deleteDirectory(FolderNames.INSTRUMENTED_OTEL_JAR);
      FileUtils.deleteDirectory(FolderNames.JAR_WITH_INSTRUMENTED_DEPENDENCIES);
    }
  }
}
