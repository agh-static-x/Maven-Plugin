/* (C)2021 */
package agh.edu.pl.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipEntryCreator {

  public static void createZipEntryFromFile(ZipOutputStream zout, File file, String pathToFile)
      throws IOException {
    InputStream is = new FileInputStream(file);
    ZipEntry entry = new ZipEntry(pathToFile);
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
  }
}
