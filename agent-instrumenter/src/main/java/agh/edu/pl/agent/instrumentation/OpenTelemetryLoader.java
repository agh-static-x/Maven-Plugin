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
  private String pluginResourcesPath;
  private final String OTEL_AGENT_NAME = "io.opentelemetry.javaagent.OpenTelemetryAgent";

  public OpenTelemetryLoader(String otelJarPath, String pluginResourcesPath) {
    this.otelJarPath = otelJarPath;
    this.pluginResourcesPath = pluginResourcesPath;
  }

  public void instrument() {
    loadOtel(new File(otelJarPath));
    File otelJarFile = new File(otelJarPath);
    File copyFile =
        new File(
            pluginResourcesPath
                + System.getProperty("file.separator")
                + otelJarFile.getName());
    try {
      Files.copy(otelJarFile.toPath(), copyFile.toPath());
    } catch (IOException exception) {
      System.err.println(
          "OpenTelemetry Agent JAR could not be copied to instrumentation directory.");
      return;
    }
    System.out.println("Copied OTEL to " + pluginResourcesPath);
    try {
      instrumentOpenTelemetryAgent(copyFile);
    } catch (IOException exception) {
      System.err.println("Problem occurred during OpenTelemetry Agent instrumentation.");
      return;
    }
    try {
      injectClasses(copyFile);
    } catch (IOException exception) {
      System.err.println(
          "The classes required for static instrumentation with OpenTelemetry Agent were not added properly.");
    }
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
