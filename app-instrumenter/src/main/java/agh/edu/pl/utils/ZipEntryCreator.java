/* (C)2021 */
package agh.edu.pl.utils;

import org.apache.maven.model.building.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class ZipEntryCreator {

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
        logger.error(
            "Error (ZipException) while creating Zip Entry " + entryPath + " from file.");
      }
    }
  }

  public static File copySingleEntryFromJar(JarEntry entry, JarFile jarFile, String folderName)
      throws IOException {
    File tmpFile = new File(folderName, entry.getName());
    if (!tmpFile.getParentFile().mkdirs() && !tmpFile.getParentFile().exists()) {
      Logger logger = LoggerFactory.getLogger(ZipEntryCreator.class);
      logger.error(
          "Temporary directory for " + entry.getName() + " was not created properly.");
    }
    Files.copy(jarFile.getInputStream(entry), tmpFile.toPath());
    return tmpFile;
  }
}
