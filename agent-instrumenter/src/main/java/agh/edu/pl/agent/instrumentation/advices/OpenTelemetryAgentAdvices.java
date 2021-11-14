/* (C)2021 */
package agh.edu.pl.agent.instrumentation.advices;

import io.opentelemetry.javaagent.StaticInstrumenter;
import java.lang.instrument.Instrumentation;
import net.bytebuddy.asm.Advice;

public class OpenTelemetryAgentAdvices {

  @Advice.OnMethodExit(suppress = Throwable.class)
  static void exit(
      @Advice.Origin("#t #m") String detailedOrigin,
      @Advice.Argument(value = 1, readOnly = false) Instrumentation inst) {

    System.out.println("[INSTRUMENTATION] EXIT " + detailedOrigin);

    inst.addTransformer(StaticInstrumenter.getPostTransformer(), true);
  }
}
