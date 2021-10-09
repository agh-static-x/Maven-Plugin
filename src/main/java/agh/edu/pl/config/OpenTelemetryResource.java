/* (C)2021 */
package agh.edu.pl.config;

import java.util.Map;
import org.apache.maven.plugins.annotations.Parameter;

public class OpenTelemetryResource {

  @Parameter(property = "attributes")
  public Map attributes;

  @Parameter(property = "serviceName")
  public String serviceName;
}
