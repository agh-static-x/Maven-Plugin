/* (C)2021 */
package agh.edu.pl.config;

import org.apache.maven.plugins.annotations.Parameter;

public class BatchSpanProcessor {

  @Parameter(property = "bspScheduleDelay", defaultValue = "5000")
  public Long bspScheduleDelay;

  @Parameter(property = "bspMaxQueueSize", defaultValue = "2048")
  public Long bspMaxQueueSize;

  @Parameter(property = "bspMaxExportBatchSize", defaultValue = "512")
  public Long bspMaxExportBatchSize;

  @Parameter(property = "bspExportTimeout", defaultValue = "30000")
  public Long bspExportTimeout;
}
