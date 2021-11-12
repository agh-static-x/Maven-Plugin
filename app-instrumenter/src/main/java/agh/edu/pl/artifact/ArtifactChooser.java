/* (C)2021 */
package agh.edu.pl.artifact;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

public class ArtifactChooser {

  private final MavenProject project;
  private final String artifactName;

  public ArtifactChooser(MavenProject project, String artifactName) {
    this.project = project;
    this.artifactName = artifactName;
  }

  public List<File> chooseArtifacts() {
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
      System.out.println("Artifact with name " + artifactName + " not found.");
    } else {
      System.out.println("Artifact name not provided via parameters.");
    }

    String allArtifactsNames = Arrays.toString(artifactFiles.stream().map(File::getName).toArray());
    System.out.println(
        "Will default to instrumenting all available artifacts: " + allArtifactsNames);

    return artifactFiles;
  }
}
