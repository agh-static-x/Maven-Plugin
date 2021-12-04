/* (C)2021 */
package agh.edu.pl.utils;

import agh.edu.pl.repackaging.config.FolderNames;
import java.io.IOException;
import org.codehaus.plexus.util.FileUtils;

public class Cleanup {

  public Cleanup() {}

  public void deleteAllTemporaryFolders() {
    FolderNames folderNames = FolderNames.getInstance();
    String[] foldersToDelete = {
      folderNames.getInstrumentedOtelJarPackage(),
      folderNames.getMainJARInitialCopyPackage(),
      folderNames.getInstrumentedJARPackage(),
      folderNames.getOpenTelemetryClassesPackage(),
      folderNames.getJARWithInstrumentedDependenciesPackage(),
      folderNames.getFrameworkSupportFolder()
    };
    try {
      for (String folderName : foldersToDelete) {
        FileUtils.forceDelete(folderName);
      }
    } catch (IllegalArgumentException ignored) {
    } catch (IOException exception) {
      System.err.println("Temporary directories were not deleted properly.");
    }
  }
}
