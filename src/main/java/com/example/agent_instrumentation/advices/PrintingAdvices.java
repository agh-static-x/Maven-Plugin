package com.example.agent_instrumentation.advices;

import net.bytebuddy.asm.Advice;

public class PrintingAdvices {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    static long enter(@Advice.Origin String origin,
                      @Advice.Origin("#t #m") String detailedOrigin) {
        System.out.println("[INSTRUMENTATION] ENTER " + detailedOrigin);

        return System.nanoTime();

    }

    @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class)
    static void exit(@Advice.Origin String origin,
                     @Advice.Origin("#t #m") String detailedOrigin) {
        System.out.println("[INSTRUMENTATION] EXIT " + detailedOrigin);
    }
}
