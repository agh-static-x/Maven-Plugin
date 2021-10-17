/* (C)2021 */
package agh.edu.pl.agent.instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.*;

import agh.edu.pl.agent.instrumentation.advices.InstallBootstrapJarAdvice;
import agh.edu.pl.agent.instrumentation.advices.OpenTelemetryAgentAdvices;
import io.opentelemetry.javaagent.BytesAndName;
import io.opentelemetry.javaagent.PostTransformer;
import io.opentelemetry.javaagent.PreTransformer;
import io.opentelemetry.javaagent.StaticInstrumenter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.List;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;

public class OpenTelemetryLoader {
  public URLClassLoader otelClassLoader;
  public Class<?> openTelemetryAgentClass;
  public String otelJarPath;

  public final String OTEL_AGENT_NAME = "io.opentelemetry.javaagent.OpenTelemetryAgent";
  public final String TMP_DIR = "INSTRUMENTED_OTEL";

  public OpenTelemetryLoader(String otelJarPath) {
    this.otelJarPath = otelJarPath;
  }

  public void instrument() throws IOException {
    loadOtel(new File(otelJarPath));
    File tmpDir = new File(TMP_DIR);
    tmpDir.mkdir();
    File copyFile = new File(TMP_DIR + System.getProperty("file.separator") + otelJarPath);
    Files.copy(new File(otelJarPath).toPath(), copyFile.toPath());
    System.out.println("Copied OTEL to " + TMP_DIR);
    instrumentOpenTelemetryAgent(copyFile);
    injectClasses(copyFile);
  }

  public synchronized void loadOtel(File otelJar) {
    try {
      otelClassLoader =
          new URLClassLoader(
              new URL[] {otelJar.toURI().toURL()}, OpenTelemetryLoader.class.getClassLoader());

      openTelemetryAgentClass = Class.forName(OTEL_AGENT_NAME, true, otelClassLoader);
      System.out.println("Loaded OpenTelemetryAgent: " + openTelemetryAgentClass);
    } catch (ClassNotFoundException | IOException e) {
      e.printStackTrace();
    }
  }

  public void instrumentOpenTelemetryAgent(File jarFile) throws IOException {
    new ByteBuddy()
        .rebase(openTelemetryAgentClass)
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
