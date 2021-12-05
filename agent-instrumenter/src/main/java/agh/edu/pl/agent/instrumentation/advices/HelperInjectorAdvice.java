package agh.edu.pl.agent.instrumentation.advices;

import io.opentelemetry.javaagent.StaticInstrumenter;
import java.util.Map;
import net.bytebuddy.asm.Advice;

public class HelperInjectorAdvice {

  @Advice.OnMethodEnter(suppress = Throwable.class)
  static void enter(@Advice.Argument(value = 0) Map<String, byte[]> classnameToBytes) {

    for (String className : classnameToBytes.keySet()) {
      String modifiedClassName = className.replace(".", "/") + ".class";
      StaticInstrumenter.AdditionalClasses.put(modifiedClassName, classnameToBytes.get(className));
    }
  }
}
