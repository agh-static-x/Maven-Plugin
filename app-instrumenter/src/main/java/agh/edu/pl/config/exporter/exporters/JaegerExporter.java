/* (C)2021 */
package agh.edu.pl.config.exporter.exporters;

import agh.edu.pl.config.exporter.ExporterType;
import org.apache.maven.plugins.annotations.Parameter;

public class JaegerExporter implements IExporter {

  @Parameter(property = "tracesExporter", defaultValue = "jaeger")
  public ExporterType tracesExporter;

  @Parameter(property = "exporterJaegerEndpoint")
  public String exporterJaegerEndpoint;

  @Parameter(property = "exporterJaegerTimeout")
  public String exporterJaegerTimeout;
}
