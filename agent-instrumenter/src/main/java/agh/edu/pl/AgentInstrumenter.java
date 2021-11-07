/* (C)2021 */
package agh.edu.pl;

import agh.edu.pl.agent.instrumentation.OpenTelemetryLoader;

public class AgentInstrumenter {
  public static void main(String[] args) {
    String otelJarPath, pluginResourcesPath;
    if(args.length >= 2) {
      otelJarPath = args[0];
      pluginResourcesPath = args[1];
    } else {
      otelJarPath = "opentelemetry-javaagent-all.jar";
      pluginResourcesPath = "app-instrumenter/src/main/resources";
    }
    OpenTelemetryLoader loader = new OpenTelemetryLoader(otelJarPath, pluginResourcesPath);
    loader.instrument();
  }
}
