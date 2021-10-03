/* (C)2021 */
package agh.edu.pl;

import agh.edu.pl.agent.instrumentation.OpenTelemetryLoader;
import agh.edu.pl.dependency.JarRepackager;
import java.io.IOException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "dependency-inspection", defaultPhase = LifecyclePhase.PACKAGE)
public class DependencyInspectorMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  MavenProject project;

  @Parameter(property = "agentPath", defaultValue = "opentelemetry-javaagent-all.jar")
  private String agentPath;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    OpenTelemetryLoader loader = new OpenTelemetryLoader(agentPath);
    try {
      loader.instrument();
    } catch (IOException exception) {
      exception.printStackTrace();
    }
    JarRepackager repackager = new JarRepackager(project, agentPath);
    try {
      repackager.listArtifacts();
      repackager.instrumentDependencies();
      repackager.instrumentMain();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
