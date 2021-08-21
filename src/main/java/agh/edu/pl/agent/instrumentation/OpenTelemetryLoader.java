/* (C)2021 */
package agh.edu.pl.agent.instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;

import agh.edu.pl.agent.instrumentation.advices.InstallBootstrapJarAdvice;
import agh.edu.pl.agent.instrumentation.advices.OpenTelemetryAgentAdvices;
import io.opentelemetry.javaagent.BytesAndName;
import io.opentelemetry.javaagent.PostTransformer;
import io.opentelemetry.javaagent.PreTransformer;
import io.opentelemetry.javaagent.StaticInstrumenter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;

public class OpenTelemetryLoader {

  private Class<?> otelAgentClass;

  private final String otelJarPath;
  private static final String OTEL_AGENT_NAME = "io.opentelemetry.javaagent.OpenTelemetryAgent";
  private static final String TMP_DIR = "tmpOtel";

  public OpenTelemetryLoader(String otelJarPath) {
    this.otelJarPath = otelJarPath;
  }

  public void instrument() throws IOException {
    otelAgentClass = JarUtils.loadClassFromJar(otelJarPath, OTEL_AGENT_NAME);
    File copiedOtel = JarUtils.copyFile(otelJarPath, TMP_DIR);
    instrumentOpenTelemetryAgent(copiedOtel);
    injectClasses(copiedOtel);
  }

  public void instrumentOpenTelemetryAgent(File jarFile) throws IOException {
    new ByteBuddy()
        .rebase(otelAgentClass)
        //            .visit(Advice.to(PrintingAdvices.class).on(isMethod()))
        .visit(
            Advice.to(OpenTelemetryAgentAdvices.class)
                .on(
                    isMethod().and(named("agentmain"))
                    //                                .and(takesArguments(2))
                    ))
        .visit(
            Advice.to(InstallBootstrapJarAdvice.class)
                .on(isMethod().and(named("installBootstrapJar"))))
        .make()
        .inject(jarFile);
  }

  public void injectClasses(File jarFile) throws IOException {
    List<Class<?>> classesToInject =
        List.of(
            BytesAndName.class,
            PreTransformer.class,
            PostTransformer.class,
            StaticInstrumenter.class);

    for (var clazz : classesToInject) {
      String[] clazzNameParts = clazz.getName().split("\\.");
      String clazzName = clazzNameParts[clazzNameParts.length - 1];

      String fullclazzName = "io.opentelemetry.javaagent." + clazzName;

      new ByteBuddy().rebase(clazz).name(fullclazzName).make().inject(jarFile);
      System.out.println("Instrumented " + fullclazzName);
    }
  }
}
