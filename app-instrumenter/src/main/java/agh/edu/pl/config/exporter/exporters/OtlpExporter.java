/* (C)2021 */
package agh.edu.pl.config.exporter.exporters;

import agh.edu.pl.config.exporter.ExporterType;
import java.util.Map;
import org.apache.maven.plugins.annotations.Parameter;

public class OtlpExporter implements IExporter {

  @Parameter(property = "tracesExporter", defaultValue = "otlp")
  public ExporterType tracesExporter;

  @Parameter(property = "metricExporter", defaultValue = "otlp")
  public ExporterType metricExporter;

  @Parameter(property = "exporterOtlpEndpoint")
  public String exporterOtlpEndpoint;

  @Parameter(property = "exporterOtlpTracesEndpoint")
  public String exporterOtlpTracesEndpoint;

  @Parameter(property = "exporterOtlpMetricsEndpoint")
  public String exporterOtlpMetricsEndpoint;

  @Parameter(property = "exporterOtlpCertificate")
  public String exporterOtlpCertificate;

  @Parameter(property = "exporterOtlpTracesCertificate")
  public String exporterOtlpTracesCertificate;

  @Parameter(property = "exporterOtlpMetricsCertificate")
  public String exporterOtlpMetricsCertificate;

  @Parameter(property = "exporterOtlpHeaders")
  public Map exporterOtlpHeaders;

  @Parameter(property = "exporterOtlpTracesHeaders")
  public Map exporterOtlpTracesHeaders;

  @Parameter(property = "exporterOtlpMetricsHeaders")
  public Map exporterOtlpMetricsHeaders;

  @Parameter(property = "exporterOtlpCompression")
  public String exporterOtlpCompression;

  @Parameter(property = "exporterOtlpTracesCompression")
  public String exporterOtlpTracesCompression;

  @Parameter(property = "exporterOtlpMetricsCompression")
  public String exporterOtlpMetricsCompression;

  @Parameter(property = "exporterOtlpTimeout")
  public Long exporterOtlpTimeout;

  @Parameter(property = "exporterOtlpTracesTimeout")
  public Long exporterOtlpTracesTimeout;

  @Parameter(property = "exporterOtlpMetricsTimeout")
  public Long exporterOtlpMetricsTimeout;

  @Parameter(property = "exporterOtlpProtocol", defaultValue = "grpc")
  public String exporterOtlpProtocol;

  @Parameter(property = "exporterOtlpTracesProtocol", defaultValue = "grpc")
  public String exporterOtlpTracesProtocol;

  @Parameter(property = "exporterOtlpMetricsProtocol", defaultValue = "grpc")
  public String exporterOtlpMetricsProtocol;
}
