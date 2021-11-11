/* (C)2021 */
package agh.edu.pl.repackaging.config;

public class InstrumentationConstants {
  public static final String STATIC_INSTRUMENTER_CLASS =
      "io.opentelemetry.javaagent.StaticInstrumenter";
  public static final String OTEL_AGENT_JAR_FILENAME = "opentelemetry-javaagent-all.jar";

  public static ProcessBuilder getInstrumentationProcess(
      String agentPath, String classpath, String folderName) {
    return new ProcessBuilder(
        "java",
        "-Dota.static.instrumenter=true",
        "-Dotel.javaagent.experimental.field-injection.enabled=false",
        String.format("-javaagent:%s", agentPath),
        "-cp",
        String.format("%s", classpath),
        InstrumentationConstants.STATIC_INSTRUMENTER_CLASS,
        folderName);
  }
}