/* (C)2021 */
package agh.edu.pl.artifact;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains methods to retrieve all artifacts created in Maven project.
 */
public class ArtifactChooser {

  private final MavenProject project;
  private final String artifactName;
  private final Logger logger = LoggerFactory.getLogger(ArtifactChooser.class);

  public ArtifactChooser(MavenProject project, String artifactName) {
    this.project = project;
    this.artifactName = artifactName;
  }

  /**
   * Returns a list of all Maven project artifacts (attached included) or artifact specified by user.
   *
   * @return list of File objects that represent artifacts retrieved from Maven project. May contains only one File,
   *         if user specified the artifact name
   */
  public List<File> chooseArtifact() {
    ArrayList<File> artifactFiles = new ArrayList<>();
    artifactFiles.add(project.getArtifact().getFile());
    List<Artifact> attachedArtifacts = project.getAttachedArtifacts();

    if (attachedArtifacts.isEmpty()) {
      return Collections.singletonList(artifactFiles.get(0));
    } else {
      attachedArtifacts.stream().map(Artifact::getFile).forEach(artifactFiles::add);
    }

    if (artifactName != null) {
      for (var artifactFile : artifactFiles) {
        if (artifactFile.getName().equals(artifactName)) {
          return Collections.singletonList(artifactFile);
        }
      }
      logger.debug("Artifact with name " + artifactName + " not found.");
    } else {
      logger.debug("Artifact name not provided via parameters.");
    }

    String allArtifactsNames = Arrays.toString(artifactFiles.stream().map(File::getName).toArray());
    logger.debug("Will default to instrumenting all available artifacts: " + allArtifactsNames);

    return artifactFiles;
  }
}
