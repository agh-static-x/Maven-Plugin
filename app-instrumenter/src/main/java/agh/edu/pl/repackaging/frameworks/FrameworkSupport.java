/* (C)2021 */
package agh.edu.pl.repackaging.frameworks;

import static agh.edu.pl.utils.ZipEntryCreator.copySingleEntryFromJar;
import static agh.edu.pl.utils.ZipEntryCreator.createZipEntryFromFile;

import agh.edu.pl.repackaging.config.TemporaryFolders;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipOutputStream;

/** Contains methods supporting copying JAR entries with or without prefix specific to framework. */
public class FrameworkSupport {

  private final String classesPrefix;
  private final String libPrefix;
  private final HashSet<String> filesToRepackage = new HashSet<>();

  public FrameworkSupport(String classesPrefix, String libPrefix) {
    this.classesPrefix = classesPrefix;
    this.libPrefix = libPrefix;
  }

  public String getClassesPrefix() {
    return classesPrefix;
  }

  public String getLibPrefix() {
    return libPrefix;
  }

  public void addFileToRepackage(String fileName) {
    this.filesToRepackage.add(fileName);
  }

  public HashSet<String> getFilesToRepackage() {
    return filesToRepackage;
  }

  /**
   * Copies the entry without the prefix specific to framework. To achieve that, first copies the
   * entry to temporary folder and then stores the unpacked entry under a different path in output
   * JAR.
   *
   * @param entry JarEntry that is being copied
   * @param zout ZipOutputStream of the file entry is being copied to
   * @param inputFile file that contains the entry
   * @throws IOException If the exception is thrown
   * @see ZipOutputStream
   */
  public void copyMainClassWithoutPrefix(JarEntry entry, ZipOutputStream zout, JarFile inputFile)
      throws IOException {
    File tmpFile =
        copySingleEntryFromJar(
            entry, inputFile, TemporaryFolders.getInstance().getFrameworkSupportFolder());
    String newEntryPath = entry.getName().replace(getClassesPrefix(), "");
    addFileToRepackage(newEntryPath);
    createZipEntryFromFile(zout, tmpFile, newEntryPath);
  }
}
