/* (C)2021 */
package agh.edu.pl.config.exporter;

import agh.edu.pl.config.exporter.exporters.*;
import org.apache.maven.plugins.annotations.Parameter;

public class Exporter {

  @Parameter(property = "otlpExporter")
  public OtlpExporter otlpExporter;

  @Parameter(property = "jaegerExporter")
  public JaegerExporter jaegerExporter;

  @Parameter(property = "zipkinExporter")
  public ZipkinExporter zipkinExporter;

  @Parameter(property = "prometheusExporter")
  public PrometheusExporter prometheusExporter;

  @Parameter(property = "prometheusExporter")
  public LoggingExporter loggingExporter;
}
