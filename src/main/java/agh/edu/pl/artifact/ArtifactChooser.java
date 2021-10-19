/* (C)2021 */
package agh.edu.pl.artifact;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

public class ArtifactChooser {

  private final MavenProject project;

  public ArtifactChooser(MavenProject project) {
    this.project = project;
  }

  public File chooseArtifacts() {
    ArrayList<File> artifacts = new ArrayList<>();
    artifacts.add(project.getArtifact().getFile());
    List<Artifact> attachedArtifacts = project.getAttachedArtifacts();
    if (attachedArtifacts.isEmpty()) {
      return artifacts.get(0);
    } else {
      attachedArtifacts.forEach((Artifact artifact) -> artifacts.add(artifact.getFile()));
    }
    System.out.println("Choose the JAR file you want to add telemetry to:");
    for (int i = 0; i < artifacts.size(); i++) {
      System.out.println(i + " - " + artifacts.get(i).getName());
    }
    Scanner scanner = new Scanner(System.in);
    int number = scanner.nextInt();
    while (number < 0 || number > artifacts.size() - 1) {
      System.out.println("Number is out of scope. Please enter another number.");
      number = scanner.nextInt();
    }
    return artifacts.get(number);
  }
}
