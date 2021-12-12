/* (C)2021 */
package agh.edu.pl;

import agh.edu.pl.agent.instrumentation.OpenTelemetryLoader;

public class AgentInstrumenter {
  /**
   * The main method of agent-instrumenter module. It validates the passed arguments, starts the
   * loading of the OpenTelemetry javaagent JAR file, and then starts the instrumentation process.
   *
   * @param args First argument is the path to the OpenTelemetry javaagent JAR file. Second argument
   *     is the path to the resources folder in app-instrumenter module.
   */
  public static void main(String[] args) {
    String otelJarPath, pluginResourcesPath;
    if (args.length >= 2) {
      otelJarPath = args[0];
      pluginResourcesPath = args[1];
    } else {
      otelJarPath = "opentelemetry-javaagent.jar";
      pluginResourcesPath = "app-instrumenter/src/main/resources";
    }
    OpenTelemetryLoader loader = new OpenTelemetryLoader(otelJarPath, pluginResourcesPath);
    loader.instrument();
  }
}
