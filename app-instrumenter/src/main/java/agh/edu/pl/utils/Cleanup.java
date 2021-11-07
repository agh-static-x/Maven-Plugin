/* (C)2021 */
package agh.edu.pl.utils;

import agh.edu.pl.repackaging.config.FolderNames;
import java.io.IOException;
import org.codehaus.plexus.util.FileUtils;

public class Cleanup {

  public Cleanup() {}

  public void deleteAllTemporaryFolders() {
    FolderNames folderNames = FolderNames.getInstance();
    try {
      FileUtils.deleteDirectory(folderNames.getInstrumentedOtelJarPackage());
      FileUtils.deleteDirectory(folderNames.getMainJARInitialCopyPackage());
      FileUtils.deleteDirectory(folderNames.getInstrumentedDependencyPackage());
      FileUtils.deleteDirectory(folderNames.getInstrumentedJARPackage());
      FileUtils.deleteDirectory(folderNames.getOpenTelemetryClassesPackage());
      FileUtils.deleteDirectory(folderNames.getJARWithInstrumentedDependenciesPackage());
    } catch (IllegalArgumentException ignored) {
    } catch (IOException exception) {
      System.err.println("Temporary directories were not deleted properly.");
    }
  }
}
