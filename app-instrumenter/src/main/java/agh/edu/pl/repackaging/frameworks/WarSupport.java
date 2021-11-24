package agh.edu.pl.repackaging.frameworks;

import java.util.HashSet;

public class WarSupport implements FrameworkSupport {
  private final HashSet<String> filesToRepackage = new HashSet<>();

  @Override
  public String getClassesPrefix() {
    return "WEB-INF/classes/";
  }

  @Override
  public String getLibPrefix() {
    return "WEB-INF/lib/";
  }

  @Override
  public void addFileToRepackage(String fileName) {
    filesToRepackage.add(fileName);
  }

  @Override
  public HashSet<String> getFilesToRepackage() {
    return filesToRepackage;
  }
}
