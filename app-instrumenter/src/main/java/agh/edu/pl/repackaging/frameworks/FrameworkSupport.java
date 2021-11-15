/* (C)2021 */
package agh.edu.pl.repackaging.frameworks;

import java.util.HashSet;
import java.util.jar.JarFile;

public interface FrameworkSupport {
  String getPrefix();

  void addFileToRepackage(String fileName);

  HashSet<String> getFilesToRepackage();
}
