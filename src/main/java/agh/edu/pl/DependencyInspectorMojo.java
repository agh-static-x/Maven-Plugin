/* (C)2021 */
package agh.edu.pl;

import agh.edu.pl.agent.instrumentation.OpenTelemetryLoader;
import agh.edu.pl.config.Exporter;
import agh.edu.pl.config.Propagator;
import agh.edu.pl.dependency.DependenciesGatherer;
import java.io.IOException;
import java.util.Map;

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



  // agent path ↓

  @Parameter(property = "agentPath", defaultValue = "opentelemetry-javaagent-all.jar")
  private String agentPath;



  // exporter ↓

  @Parameter(property = "exporterType", defaultValue = "OTLP")
  private Exporter exporterType;

  @Parameter(property = "metricExporterEndpoint")
  private String metricExporterEndpoint;

  @Parameter(property = "traceExporterEndpoint")
  private String traceExporterEndpoint;

  @Parameter(property = "exporterHeader")
  private Map exporterHeader;

  @Parameter(property = "exporterTimeout")
  private Long exporterTimeout;

  @Parameter(property = "zipkinPort")
  private int zipkinPort;

  @Parameter(property = "zipkinHost")
  private String zipkinHost;

  @Parameter(property = "traceExporterEndpoint", defaultValue = "true")
  private boolean metricExporterOn;

  @Parameter(property = "traceExporterEndpoint", defaultValue = "true")
  private boolean traceExporterOn;

  @Parameter(property = "spanNamePrefix")
  private String spanNamePrefix;



  // propagator ↓

  @Parameter(property = "propagator")
  private Propagator[] propagator;



  // OpenTelemetry Resource ↓

  @Parameter(property = "openTelemetryResource")
  private Map openTelemetryResource;



  // span packet processor ↓

  @Parameter(property = "spanPacketProcessorDelay", defaultValue = "5000")
  private Long spanPacketProcessorDelay;

  @Parameter(property = "spanPacketProcessorMaxQueueSize", defaultValue = "2048")
  private Long spanPacketProcessorMaxQueueSize;

  @Parameter(property = "spanPacketProcessorMaxPacketSize", defaultValue = "512")
  private Long spanPacketProcessorMaxPacketSize;

  @Parameter(property = "spanPacketProcessorMaxExportTime", defaultValue = "3000")
  private Long spanPacketProcessorMaxExportTime;



  // sampler ↓

  @Parameter(property = "samplerAlwaysOn")
  private boolean samplerAlwaysOn;

  @Parameter(property = "samplerAlwaysOf")
  private boolean samplerAlwaysOf;

  @Parameter(property = "samplerTraceIdRatio")
  private double samplerTraceIdRatio;

  @Parameter(property = "samplerParentBasedAlwaysOn")
  private boolean samplerParentBasedAlwaysOn;

  @Parameter(property = "samplerParentBasedAlwaysOff")
  private boolean samplerParentBasedAlwaysOff;

  @Parameter(property = "samplerParentBasedTraceIdRatio")
  private double samplerParentBasedTraceIdRatio;



  // span limits ↓

  @Parameter(property = "maxSpanAttributeNumber", defaultValue = "128")
  private int maxSpanAttributeNumber;

  @Parameter(property = "maxSpanEventNumber", defaultValue = "128")
  private int maxSpanEventNumber;

  @Parameter(property = "maxSpanLinkNumber", defaultValue = "128")
  private int maxSpanLinkNumber;



  // Interval Metric Reader ↓

  @Parameter(property = "intervalMetricReader", defaultValue = "60000")
  private Long intervalMetricReader;



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
