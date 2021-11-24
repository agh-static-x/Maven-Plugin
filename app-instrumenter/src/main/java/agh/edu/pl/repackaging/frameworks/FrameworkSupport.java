/* (C)2021 */
package agh.edu.pl.repackaging.frameworks;

import static agh.edu.pl.utils.ZipEntryCreator.copySingleEntryFromJar;
import static agh.edu.pl.utils.ZipEntryCreator.createZipEntryFromFile;

import agh.edu.pl.repackaging.config.FolderNames;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipOutputStream;

public interface FrameworkSupport {
  String getClassesPrefix();
  String getLibPrefix();

  void addFileToRepackage(String fileName);

  HashSet<String> getFilesToRepackage();

  default void copyMainClassWithoutPrefix(JarEntry entry, ZipOutputStream zout, JarFile jarFile)
      throws IOException {
    File tmpFile =
        copySingleEntryFromJar(
            entry, jarFile, FolderNames.getInstance().getFrameworkSupportFolder());
    String newEntryPath = entry.getName().replace(getClassesPrefix(), "");
    addFileToRepackage(newEntryPath);
    createZipEntryFromFile(zout, tmpFile, newEntryPath);
  }
}
