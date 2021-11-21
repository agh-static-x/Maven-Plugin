/* (C)2021 */
package agh.edu.pl.repackaging.frameworks;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AppFramework {
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
      if (entry.isDirectory() && entry.getName().equals("BOOT-INF/")) {
        return new SpringSupport();
      }
      if (entry.isDirectory() && entry.getName().equals("WEB-INF/")) {
        return new WarSupport();
      }
    }
    return null;
  }
}
