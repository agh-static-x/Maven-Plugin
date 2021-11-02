/* (C)2021 */
package agh.edu.pl.config.exporter.exporters;

import agh.edu.pl.config.exporter.ExporterType;
import org.apache.maven.plugins.annotations.Parameter;

public class ZipkinExporter implements IExporter {

  @Parameter(property = "tracesExporter", defaultValue = "zipkin")
  public ExporterType tracesExporter;

  @Parameter(property = "exporterZipkinEndpoint")
  public String exporterZipkinEndpoint;
}
