/* (C)2021 */
package agh.edu.pl.config.exporter.exporters;

import agh.edu.pl.config.exporter.ExporterType;
import org.apache.maven.plugins.annotations.Parameter;

public class LoggingExporter implements IExporter {

  @Parameter(property = "tracesExporter", defaultValue = "logging")
  public ExporterType tracesExporter;

  @Parameter(property = "metricExporter", defaultValue = "logging")
  public ExporterType metricExporter;

  @Parameter(property = "exporterLoggingPrefix")
  public String exporterLoggingPrefix;
}
