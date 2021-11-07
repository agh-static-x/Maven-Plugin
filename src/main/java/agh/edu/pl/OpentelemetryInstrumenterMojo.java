/* (C)2021 */
package agh.edu.pl;

import agh.edu.pl.artifact.ArtifactChooser;
import agh.edu.pl.repackaging.JarRepackager;
import agh.edu.pl.utils.Cleanup;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "instrument-with-opentelemetry", defaultPhase = LifecyclePhase.PACKAGE)
public class OpentelemetryInstrumenterMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    JarRepackager repackager = new JarRepackager();
    try {
      repackager.setJarFile(new ArtifactChooser(project).chooseArtifacts());
      repackager.checkFrameworkSupport();
      repackager.repackageJar();
      repackager.addOpenTelemetryClasses();
    } finally {
      new Cleanup().deleteAllTemporaryFolders();
    }
  }
}
