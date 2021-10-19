/* (C)2021 */
package agh.edu.pl.repackaging.instrumenters.dependencies;

import static agh.edu.pl.utils.ZipEntryCreator.createZipEntryFromFile;

import agh.edu.pl.repackaging.config.FolderNames;
import agh.edu.pl.repackaging.config.InstrumentationConstants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

  public DependenciesInstrumenter(File file, String agentPath) {
    this.file = file;
    this.agentPath = agentPath;
  }

  public void instrumentDependencies() throws Exception {
    try {
      File tmpDir = new File(FolderNames.MAIN_JAR_INITIAL_COPY);
      tmpDir.mkdir();
      File instrumentedDir = new File(FolderNames.JAR_WITH_INSTRUMENTED_DEPENDENCIES);
      instrumentedDir.mkdir();
      File instrumentedFileDirectory = new File(FolderNames.INSTRUMENTED_DEPENDENCY);
      instrumentedFileDirectory.mkdir();

      JarFile jarFile = null;
      try {
        jarFile = new JarFile(this.file);
      } catch (IOException e) {
        e.printStackTrace();
      }

      if (jarFile == null) {
        System.err.println("Error while getting project jar file.");
        return;
      }
      String pattern = Pattern.quote(System.getProperty("file.separator"));
      String[] outFileNameParts = jarFile.getName().split(pattern);
      final String outFileName = outFileNameParts[outFileNameParts.length - 1];
      final File outFile = new File(FolderNames.JAR_WITH_INSTRUMENTED_DEPENDENCIES, outFileName);
      final ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(outFile));
      zout.setMethod(ZipOutputStream.STORED);

      for (Enumeration<JarEntry> enums = jarFile.entries(); enums.hasMoreElements(); ) {
        JarEntry entry = enums.nextElement();
        if (entry.getName().endsWith(".jar")) {
          StringBuilder classpath = new StringBuilder();
          String fileName = FolderNames.MAIN_JAR_INITIAL_COPY + "/" + entry.getName();
          File f = new File(fileName);
          f.getParentFile().mkdirs();
          Files.copy(jarFile.getInputStream(entry), f.toPath());
          classpath.append(fileName).append(File.pathSeparator);
          System.out.println(classpath);
          Process process =
              InstrumentationConstants.getInstrumentationProcess(
                      agentPath, classpath.toString(), FolderNames.INSTRUMENTED_DEPENDENCY)
                  .inheritIO()
                  .start();
          int ret = process.waitFor();
          String[] fileNameParts = entry.getName().split("/");
          createZipEntryFromFile(
              zout,
              new File(
                  FolderNames.INSTRUMENTED_DEPENDENCY
                      + "/"
                      + fileNameParts[fileNameParts.length - 1]),
              entry.getName());
          FileUtils.cleanDirectory(FolderNames.MAIN_JAR_INITIAL_COPY);
          FileUtils.cleanDirectory(FolderNames.INSTRUMENTED_DEPENDENCY);
        } else {
          ZipEntry outEntry = new ZipEntry(entry);
          zout.putNextEntry(outEntry);
          InputStream in = jarFile.getInputStream(entry);
          in.transferTo(zout);
          in.close();
          zout.closeEntry();
        }
      }
      zout.close();
      jarFile.close();
    } finally {
      FileUtils.deleteDirectory(FolderNames.MAIN_JAR_INITIAL_COPY);
      FileUtils.deleteDirectory(FolderNames.INSTRUMENTED_DEPENDENCY);
    }
  }
}
