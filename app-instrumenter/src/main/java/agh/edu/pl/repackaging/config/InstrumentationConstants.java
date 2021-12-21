/* (C)2021 */
package agh.edu.pl.repackaging.config;

/** Contains the constants required for the instrumentation process. */
public class InstrumentationConstants {
  public static final String STATIC_INSTRUMENTER_CLASS =
      "io.opentelemetry.javaagent.StaticInstrumenter";
  public static final String OTEL_AGENT_JAR_FILENAME = "opentelemetry-javaagent.jar";

  /**
   * Returns the ProcessBuilder for instrumentation process.
   *
   * @param agentPath path to the OpenTelemetry javaagent JAR
   * @param classpath classpath for instrumentation process (list of JARs to instrument, separated
   *     with path separator)
   * @param folderName output folder name
   * @param transitiveDependencies list of transitive dependencies separated with path separator
   * @return ProcessBuilder for instrumentation process
   * @see ProcessBuilder
   */
  public static ProcessBuilder getInstrumentationProcess(
      String agentPath, String classpath, String folderName, String transitiveDependencies) {
    return new ProcessBuilder(
        "java",
        "-Dota.static.instrumenter=true",
        "-Dotel.instrumentation.internal-class-loader.enabled=false",
        String.format("-javaagent:%s", agentPath),
        "-cp",
        String.format("%s", classpath),
        InstrumentationConstants.STATIC_INSTRUMENTER_CLASS,
        folderName,
        transitiveDependencies);
  }
}
