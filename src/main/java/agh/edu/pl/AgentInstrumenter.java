/* (C)2021 */
package agh.edu.pl;

import agh.edu.pl.agent.instrumentation.OpenTelemetryLoader;
import agh.edu.pl.repackaging.config.InstrumentationConstants;

public class AgentInstrumenter {
  public static void main(String[] args) {
    OpenTelemetryLoader loader =
        new OpenTelemetryLoader(InstrumentationConstants.OTEL_AGENT_JAR_FILENAME);
    loader.instrument();
  }
}
