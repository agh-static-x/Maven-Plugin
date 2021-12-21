/* (C)2021 */
package agh.edu.pl.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Contains static methods related to procedures on JAR entries. */
public class ZipEntryCreator {

  /**
   * Creates new entry from file with path in JAR specified as parameter. Reads the content of
   * provided file, then sets the proper size, compressed size and crc for the new entry. Puts the
   * entry in ZipOutputStream and closes both file and entry. If entry with the same path already
   * exists, the error is logged (but it won't affect the process).
   *
   * @param zout ZipOutputStream that entry should be transferred to
   * @param file File object representing the JAR entry content
   * @param entryPath entry path in output JAR
   * @throws IOException If there are some problems related to files read operation or putting new
   *     entry to ZipOutputStream
   * @see CRC32
   * @see ZipOutputStream
   * @see FileInputStream
   */
  public static void createZipEntryFromFile(ZipOutputStream zout, File file, String entryPath)
      throws IOException {
    try {
      Logger logger = LoggerFactory.getLogger(ZipEntryCreator.class);
      InputStream is = new FileInputStream(file);
      ZipEntry entry = new ZipEntry(entryPath);
      byte[] bytes = Files.readAllBytes(file.toPath());
      entry.setCompressedSize(file.length());
      entry.setSize(file.length());
      CRC32 crc = new CRC32();
      crc.update(bytes);
      entry.setCrc(crc.getValue());
      zout.putNextEntry(entry);
      is.transferTo(zout);
      is.close();
      zout.closeEntry();
    } catch (ZipException e) {
      if (!e.getMessage().contains("duplicate")) {
        Logger logger = LoggerFactory.getLogger(ZipEntryCreator.class);
        logger.error("Error (ZipException) while creating Zip Entry " + entryPath + " from file.");
      }
    }
  }

  /**
   * Copies the JAR entry to local directory.
   *
   * @param entry JarEntry that is being copied
   * @param jarFile file that contains the entry
   * @param folderName directory that the entry content should be copy to
   * @return File object representing the entry content
   * @throws IOException If copying the entry to directory throws I/O error
   */
  public static File copySingleEntryFromJar(JarEntry entry, JarFile jarFile, String folderName)
      throws IOException {
    File tmpFile = new File(folderName, entry.getName());
    if (!tmpFile.getParentFile().mkdirs() && !tmpFile.getParentFile().exists()) {
      Logger logger = LoggerFactory.getLogger(ZipEntryCreator.class);
      logger.error("Temporary directory for " + entry.getName() + " was not created properly.");
    }
    Files.copy(jarFile.getInputStream(entry), tmpFile.toPath());
    return tmpFile;
  }
}
