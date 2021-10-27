/* (C)2021 */
package agh.edu.pl;

import agh.edu.pl.agent.instrumentation.OpenTelemetryLoader;
import agh.edu.pl.artifact.ArtifactChooser;
import agh.edu.pl.config.*;
import agh.edu.pl.config.exporter.Exporter;
import agh.edu.pl.repackaging.JarRepackager;
import agh.edu.pl.repackaging.config.FolderNames;
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

  @Parameter(property = "agentPath", defaultValue = "opentelemetry-javaagent-all.jar")
  private String agentPath;

  @Parameter(property = "exporter")
  private Exporter exporter;

  @Parameter(property = "propagators")
  private Propagator[] propagators;

  @Parameter(property = "openTelemetryResource")
  private OpenTelemetryResource openTelemetryResource;

  @Parameter(property = "batchSpanProcessor")
  private BatchSpanProcessor batchSpanProcessor;

  @Parameter(property = "sampler")
  private Sampler sampler;

  @Parameter(property = "spanLimits")
  private SpanLimits spanLimits;

  @Parameter(property = "metricsExemplarFilter", defaultValue = "WITH_SAMPLED_TRACE")
  private ExemplarFilter metricsExemplarFilter;

  @Parameter(property = "imrExportInterval", defaultValue = "60000")
  private Long imrExportInterval;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      OpenTelemetryLoader loader = new OpenTelemetryLoader(agentPath);
      loader.instrument();
      JarRepackager repackager = new JarRepackager(agentPath);
      repackager.setJarFile(new ArtifactChooser(project).chooseArtifacts());
      repackager.repackageJar();
      repackager.addOpenTelemetryClasses();
    }finally{
      new Cleanup().deleteAllTemporaryFolders();
    }
  }
}
