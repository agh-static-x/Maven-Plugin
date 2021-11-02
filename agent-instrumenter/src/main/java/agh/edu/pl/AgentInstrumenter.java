/* (C)2021 */
package agh.edu.pl;

import agh.edu.pl.agent.instrumentation.OpenTelemetryLoader;

public class AgentInstrumenter {
  public static void main(String[] args) {
    String otelJarPath = args[0];
    String pluginResourcesPath = args[1];
    OpenTelemetryLoader loader = new OpenTelemetryLoader(otelJarPath, pluginResourcesPath);
    loader.instrument();
  }
}
