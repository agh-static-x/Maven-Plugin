package com.example.agent_instrumentation.advices;

import io.opentelemetry.javaagent.StaticInstrumenter;
import net.bytebuddy.asm.Advice;

import java.lang.instrument.Instrumentation;

public class InstallBootstrapJarAdvice {

    @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class)
    static void exit(@Advice.Origin String origin,
                     @Advice.Origin("#t #m") String detailedOrigin,
                     @Advice.Argument(value = 0, readOnly = false) Instrumentation inst) {

        System.out.println("[INSTRUMENTATION] ENTER " + detailedOrigin);

        inst.addTransformer(StaticInstrumenter.getPreTransformer());
    }
}
