/* (C)2021 */
package agh.edu.pl;

import agh.edu.pl.artifact.ArtifactChooser;
import agh.edu.pl.repackaging.JarRepackager;
import agh.edu.pl.repackaging.config.FolderNames;
import agh.edu.pl.utils.Cleanup;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(
    name = "instrument-with-opentelemetry",
    defaultPhase = LifecyclePhase.PACKAGE,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class OpentelemetryInstrumenterMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  @Parameter(readonly = true)
  private String artifactName;

  @Parameter(readonly = true)
  private String outputFolder;

  @Parameter(readonly = true, defaultValue = "-instrumented")
  private String suffix;

  @Parameter(readonly = true, defaultValue = "false")
  private boolean noSuffix;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    Set<Artifact> artifactSet = project.getArtifacts();
    HashMap<Artifact, Boolean> artifactsMap = new HashMap<>();
    artifactSet.forEach((artifact) -> artifactsMap.put(artifact, true));
    JarRepackager repackager = new JarRepackager();
    if (outputFolder != null) FolderNames.getInstance().setFinalFolder(outputFolder);
    else FolderNames.getInstance().setFinalFolder(project.getBuild().getDirectory());
    try {
      List<File> artifactsToInstrument =
          new ArtifactChooser(project, artifactName).chooseArtifacts();
      for (File artifact : artifactsToInstrument) {
        System.out.println("Instrumenting artifact " + artifact.getName());
        repackager.setJarFile(artifact);
        repackager.checkFrameworkSupport();
        repackager.repackageJar(artifactsMap);
        String suf = noSuffix ? "" : suffix;
        repackager.addOpenTelemetryClasses(suf);
      }
    } finally {
      new Cleanup().deleteAllTemporaryFolders();
    }
  }
}
