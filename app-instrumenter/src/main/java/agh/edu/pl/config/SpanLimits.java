/* (C)2021 */
package agh.edu.pl.config;

import org.apache.maven.plugins.annotations.Parameter;

public class SpanLimits {

  @Parameter(property = "spanAttributeValueLengthLimit")
  private int spanAttributeValueLengthLimit;

  @Parameter(property = "spanAttributeCountLimit", defaultValue = "128")
  private int spanAttributeCountLimit;

  @Parameter(property = "spanEventCountLimit", defaultValue = "128")
  private int spanEventCountLimit;

  @Parameter(property = "spanLinkCountLimit", defaultValue = "128")
  private int spanLinkCountLimit;
}
