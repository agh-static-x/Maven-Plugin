/* (C)2021 */
package agh.edu.pl.repackaging.frameworks;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AppFramework {
  private static final String BOOT_INF = "BOOT-INF/lib/";
  private static final String WEB_INF = "WEB-INF/lib/";
  private static final String SPRING_SUPPORT_PREFIX = "BOOT-INF/classes/";
  private static final String WAR_SUPPORT_PREFIX = "WEB-INF/classes/";
  private static final String SPRING_SUPPORT_PREFIX_LIB = "BOOT-INF/lib/";
  private static final String WAR_SUPPORT_PREFIX_LIB = "WEB-INF/lib/";

  public FrameworkSupport getAppFramework(File mainFile) {
    JarFile jarFile;
    try {
      jarFile = new JarFile(mainFile);
    } catch (IOException exception) {
      System.err.println("Error while converting File to JarFile.");
      return null;
    }
    for (Enumeration<JarEntry> enums = jarFile.entries(); enums.hasMoreElements(); ) {
      JarEntry entry = enums.nextElement();
      if (entry.isDirectory()) {
        String entryName = entry.getName();
        if (entryName.equals(BOOT_INF)) {
          return new FrameworkSupport(SPRING_SUPPORT_PREFIX, SPRING_SUPPORT_PREFIX_LIB);
        }
        if (entryName.equals(WEB_INF)) {
          return new FrameworkSupport(WAR_SUPPORT_PREFIX, WAR_SUPPORT_PREFIX_LIB);
        }
      }
    }
    return null;
  }
}
