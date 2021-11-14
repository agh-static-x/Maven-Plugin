/* (C)2021 */
package agh.edu.pl.repackaging.frameworks;

import java.util.jar.JarFile;

public interface FrameworkSupport {
  String getPrefix();

  void copyMainClassesWithoutPrefix(JarFile inputJarFile, String outputFolder, String mainFilePath);

  void addPrefixToMainClasses();
}
