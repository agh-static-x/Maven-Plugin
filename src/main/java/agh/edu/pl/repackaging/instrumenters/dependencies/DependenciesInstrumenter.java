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
  private final FolderNames folderNames = FolderNames.getInstance();

  public DependenciesInstrumenter(File file, String agentPath) {
    this.file = file;
    this.agentPath = agentPath;
  }

  public void instrumentDependencies() throws Exception {
    try {
      createInitialFolders();

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
      final File outFile =
          new File(folderNames.getJARWithInstrumentedDependenciesPackage(), outFileName);
      final ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(outFile));
      zout.setMethod(ZipOutputStream.STORED);

      for (Enumeration<JarEntry> enums = jarFile.entries(); enums.hasMoreElements(); ) {
        JarEntry entry = enums.nextElement();
        if (entry.getName().endsWith(".jar")) {
          instrumentSingleDependency(entry, jarFile, zout);
        } else {
          storeSingleJAREntry(entry, jarFile, zout);
        }
      }
      zout.close();
      jarFile.close();
    } finally {
      FileUtils.deleteDirectory(folderNames.getMainJARInitialCopyPackage());
      FileUtils.deleteDirectory(folderNames.getInstrumentedDependencyPackage());
    }
  }

  private void createInitialFolders() {
    File tmpDir = new File(folderNames.getMainJARInitialCopyPackage());
    tmpDir.mkdir();
    File instrumentedDir = new File(folderNames.getJARWithInstrumentedDependenciesPackage());
    instrumentedDir.mkdir();
    File instrumentedFileDirectory = new File(folderNames.getInstrumentedDependencyPackage());
    instrumentedFileDirectory.mkdir();
  }

  private void storeSingleJAREntry(JarEntry entry, JarFile jarFile, ZipOutputStream zout)
      throws IOException {
    ZipEntry outEntry = new ZipEntry(entry);
    zout.putNextEntry(outEntry);
    InputStream in = jarFile.getInputStream(entry);
    in.transferTo(zout);
    in.close();
    zout.closeEntry();
  }

  private void instrumentSingleDependency(JarEntry entry, JarFile jarFile, ZipOutputStream zout)
      throws IOException, InterruptedException {
    StringBuilder classpath = new StringBuilder();
    String fileName = folderNames.getMainJARInitialCopyPackage() + File.separator + entry.getName();
    File f = new File(fileName);
    f.getParentFile().mkdirs();
    Files.copy(jarFile.getInputStream(entry), f.toPath());
    classpath.append(fileName).append(File.pathSeparator);
    System.out.println(classpath);
    Process process =
        InstrumentationConstants.getInstrumentationProcess(
                agentPath, classpath.toString(), folderNames.getInstrumentedDependencyPackage())
            .inheritIO()
            .start();
    int ret = process.waitFor();
    String[] fileNameParts = entry.getName().split(File.separator);
    createZipEntryFromFile(
        zout,
        new File(
            folderNames.getInstrumentedDependencyPackage()
                + File.separator
                + fileNameParts[fileNameParts.length - 1]),
        entry.getName());
    FileUtils.cleanDirectory(folderNames.getMainJARInitialCopyPackage());
    FileUtils.cleanDirectory(folderNames.getInstrumentedDependencyPackage());
  }
}
