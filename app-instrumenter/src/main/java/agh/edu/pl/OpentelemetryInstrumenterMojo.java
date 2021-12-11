/* (C)2021 */
package agh.edu.pl;

import agh.edu.pl.artifact.ArtifactChooser;
import agh.edu.pl.repackaging.JarRepackager;
import agh.edu.pl.repackaging.config.TemporaryFolders;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mojo(
    name = "instrument",
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

  private final Logger logger = LoggerFactory.getLogger(OpentelemetryInstrumenterMojo.class);

  /**
   * Conducts the process of application target file instrumentation. Adds the shutdown hook to
   * clean the temporary directories. Sets the output folder for instrumented target file. Executes
   * the instrumentation process of artifacts chosen by the user.
   */
  @Override
  public void execute() throws MojoExecutionException {
    try {
      TemporaryFolders.create();
    } catch (IOException e) {
      throw new MojoExecutionException("Could not create required temporary folders");
    }

    Set<Artifact> artifactSet = project.getArtifacts();
    HashMap<Artifact, Boolean> artifactsMap = new HashMap<>();
    artifactSet.forEach((artifact) -> artifactsMap.put(artifact, true));

    JarRepackager repackager = new JarRepackager();
    if (outputFolder != null) TemporaryFolders.getInstance().setFinalFolder(outputFolder);
    else TemporaryFolders.getInstance().setFinalFolder(project.getBuild().getDirectory());
    List<File> artifactsToInstrument = new ArtifactChooser(project, artifactName).chooseArtifacts();
    for (File artifact : artifactsToInstrument) {
      logger.debug("Instrumenting artifact " + artifact.getName());
      repackager.setJarFile(artifact);
      repackager.checkFrameworkSupport();
      repackager.repackageJar(artifactsMap);
      String suf = noSuffix ? "" : suffix;
      repackager.addOpenTelemetryClasses(suf);
    }
  }
}
