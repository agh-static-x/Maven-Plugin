/*
 * SPDX-License-Identifier: Apache-2.0
 */
package agh.edu.pl.agent.instrumentation.advices;

import io.opentelemetry.javaagent.StaticInstrumenter;
import java.lang.instrument.Instrumentation;
import net.bytebuddy.asm.Advice;

public class InstallBootstrapJarAdvice {

  @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class)
  static void addInstrumentationEnter(
      @Advice.Origin("#t #m") String detailedOrigin,
      @Advice.Argument(value = 0, readOnly = false) Instrumentation inst) {

    System.out.println("[INSTRUMENTATION] ENTER " + detailedOrigin);

    inst.addTransformer(StaticInstrumenter.getPreTransformer());
  }
}
