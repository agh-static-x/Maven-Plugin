/* (C)2021 */
package agh.edu.pl.config;

import org.apache.maven.plugins.annotations.Parameter;

public class Sampler {
  @Parameter(property = "alwaysOn")
  private boolean alwaysOn;

  @Parameter(property = "alwaysOff")
  private boolean alwaysOff;

  @Parameter(property = "traceIdRatio")
  private double traceIdRatio;

  @Parameter(property = "parentBasedAlwaysOn")
  private boolean parentBasedAlwaysOn;

  @Parameter(property = "parentBasedAlwaysOff")
  private boolean parentBasedAlwaysOff;

  @Parameter(property = "parentBasedTraceIdRatio")
  private double parentBasedTraceIdRatio;
}
