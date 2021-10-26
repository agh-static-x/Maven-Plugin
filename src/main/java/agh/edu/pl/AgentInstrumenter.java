/* (C)2021 */
package agh.edu.pl;

import agh.edu.pl.agent.instrumentation.OpenTelemetryLoader;
import agh.edu.pl.repackaging.config.InstrumentationConstants;
import java.io.IOException;

public class AgentInstrumenter {
  public static void main(String[] args) {
    OpenTelemetryLoader loader =
        new OpenTelemetryLoader(InstrumentationConstants.OTEL_AGENT_JAR_FILENAME);
    try {
      loader.instrument();
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }
}
