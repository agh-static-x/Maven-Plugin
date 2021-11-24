/* (C)2021 */
package agh.edu.pl.repackaging.frameworks;

import java.util.HashSet;

public class SpringSupport implements FrameworkSupport {

  private final HashSet<String> filesToRepackage = new HashSet<>();

  @Override
  public String getClassesPrefix() {
    return "BOOT-INF/classes/";
  }

  @Override
  public String getLibPrefix() {
    return "BOOT-INF/lib/";
  }

  public void addFileToRepackage(String fileName) {
    this.filesToRepackage.add(fileName);
  }

  @Override
  public HashSet<String> getFilesToRepackage() {
    return filesToRepackage;
  }
}
