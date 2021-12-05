/* (C)2021 */
package agh.edu.pl.utils;

import agh.edu.pl.repackaging.config.FolderNames;
import java.io.IOException;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cleanup {
  private final Logger logger = LoggerFactory.getLogger(Cleanup.class);

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
      logger.error("Temporary directories were not deleted properly.");
    }
  }
}
