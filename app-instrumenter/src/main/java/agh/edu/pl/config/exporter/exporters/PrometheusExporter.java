/* (C)2021 */
package agh.edu.pl.config.exporter.exporters;

import agh.edu.pl.config.exporter.ExporterType;
import org.apache.maven.plugins.annotations.Parameter;

public class PrometheusExporter implements IExporter {

  @Parameter(property = "metricExporter", defaultValue = "prometheus")
  public ExporterType metricExporter;

  @Parameter(property = "prometheusPort")
  public int prometheusPort;

  @Parameter(property = "prometheusHost")
  public String prometheusHost;
}
