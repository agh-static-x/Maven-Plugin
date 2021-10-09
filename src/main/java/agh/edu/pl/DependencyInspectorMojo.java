/* (C)2021 */
package agh.edu.pl;

import agh.edu.pl.agent.instrumentation.OpenTelemetryLoader;
import agh.edu.pl.config.*;
import agh.edu.pl.config.exporter.Exporter;
import agh.edu.pl.dependency.DependenciesGatherer;
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
  private MavenProject project;

  @Parameter(property = "agentPath", defaultValue = "opentelemetry-javaagent-all.jar")
  private String agentPath;

  @Parameter(property = "exporter")
  private Exporter exporter;

  @Parameter(property = "propagator")
  private Propagator[] propagator;

  @Parameter(property = "openTelemetryResource")
  private OpenTelemetryResource openTelemetryResource;

  @Parameter(property = "batchSpanProcessor")
  private BatchSpanProcessor batchSpanProcessor;

  @Parameter(property = "sampler")
  private Sampler sampler;

  @Parameter(property = "spanLimits")
  private SpanLimits spanLimits;

  @Parameter(property = "imrExportInterval", defaultValue = "60000")
  private Long imrExportInterval;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    OpenTelemetryLoader loader = new OpenTelemetryLoader(agentPath);
    try {
      loader.instrument();
    } catch (IOException exception) {
      exception.printStackTrace();
    }
    DependenciesGatherer gatherer = new DependenciesGatherer(project, agentPath);
    try {
      gatherer.instrumentDependencies();
      gatherer.instrumentMain();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
