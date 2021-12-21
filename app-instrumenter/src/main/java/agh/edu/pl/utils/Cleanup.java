/* (C)2021 */
package agh.edu.pl.utils;

import agh.edu.pl.repackaging.config.FolderNames;
import java.io.File;
import java.io.IOException;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Contains methods related to cleanup performed after plugin processes finish. */
public class Cleanup {
  private final Logger logger = LoggerFactory.getLogger(Cleanup.class);

  public Cleanup() {}

  /**
   * Deletes all temporary directories that was created by plugin's processes. If the directories
   * were deleted beforehand, error will not be thrown.
   *
   * @see FileUtils#forceDelete(File)
   */
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
