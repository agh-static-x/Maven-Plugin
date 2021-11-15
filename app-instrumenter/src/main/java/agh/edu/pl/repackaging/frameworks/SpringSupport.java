/* (C)2021 */
package agh.edu.pl.repackaging.frameworks;

import agh.edu.pl.repackaging.config.FolderNames;
import java.util.HashSet;

public class SpringSupport implements FrameworkSupport {

  private final String prefix = "BOOT-INF/classes/";
  private final HashSet<String> filesToRepackage = new HashSet<>();

  @Override
  public String getPrefix() {
    return prefix;
  }

  public void addFileToRepackage(String fileName) {
    this.filesToRepackage.add(fileName);
  }

  @Override
  public HashSet<String> getFilesToRepackage() {
    return filesToRepackage;
  }
}
