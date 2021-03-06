/*
 * SPDX-License-Identifier: Apache-2.0
 */
package agh.edu.pl.agent.instrumentation;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;

import agh.edu.pl.agent.instrumentation.advices.HelperInjectorAdvice;
import agh.edu.pl.agent.instrumentation.advices.InstallBootstrapJarAdvice;
import agh.edu.pl.agent.instrumentation.advices.OpenTelemetryAgentAdvices;
import io.opentelemetry.javaagent.BytesAndName;
import io.opentelemetry.javaagent.PostTransformer;
import io.opentelemetry.javaagent.PreTransformer;
import io.opentelemetry.javaagent.StaticInstrumenter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.DynamicType;

/** Responsible for loading and instrumenting the OpenTelemetry javaagent JAR file */
public class OpenTelemetryLoader {
  public URLClassLoader otelClassLoader;
  private Class<?> openTelemetryAgentClass;
  private Class<?> helperInjectorClass;
  public String otelJarPath;
  private final String pluginResourcesPath;
  private final Map<String, byte[]> classesToInstrument;

  public OpenTelemetryLoader(String otelJarPath, String pluginResourcesPath) {
    this.otelJarPath = otelJarPath;
    this.pluginResourcesPath = pluginResourcesPath;
    this.classesToInstrument = new HashMap<>();
  }

  /** Conducts the process of OpenTelemetry javaagent JAR instrumentation */
  public void instrument() {
    loadClasses(new File(otelJarPath));
    File otelJarFile = new File(otelJarPath);

    System.out.println("Copied OTEL to " + pluginResourcesPath);
    try {
      instrumentOpenTelemetryAgent();
      instrumentHelperInjector();
    } catch (IOException exception) {
      exception.printStackTrace();
      System.err.println("Problem occurred during OpenTelemetry Agent instrumentation.");
      return;
    }
    prepareAdditionalClasses();
    try {
      injectClasses(otelJarFile);
    } catch (IOException exception) {
      System.err.println(
          "The classes required for static instrumentation with OpenTelemetry Agent were not added properly.");
    }
  }

  /**
   * Loads <code>io.opentelemetry.javaagent.OpenTelemetryAgent</code> class from OpenTelemetry JAR
   * file
   *
   * @param otelJar File object representing the OpenTelemetry javvagent JAR file
   */
  private synchronized void loadClasses(File otelJar) {
    AgentUtils agentUtils = null;

    try {
      agentUtils = new AgentUtils();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    try {
      otelClassLoader =
          new URLClassLoader(
              new URL[] {otelJar.toURI().toURL()}, OpenTelemetryLoader.class.getClassLoader());

      openTelemetryAgentClass =
          Class.forName("io.opentelemetry.javaagent.OpenTelemetryAgent", true, otelClassLoader);
      System.out.println("Loaded OpenTelemetryAgent: " + openTelemetryAgentClass);

      Path tmpDir = agentUtils.extractAgent(otelJar);
      loadHelperInjector(tmpDir);
      System.out.println("Loaded HelperInjector: " + helperInjectorClass);

    } catch (ClassNotFoundException | IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Loads io.opentelemetry.javaagent.tooling.HelperInjector class from OpenTelemetry javaagent JAR
   * file
   *
   * @param tmpDir Path to temporary directory with copied OpenTelemetry javaagent JAR file
   */
  private synchronized void loadHelperInjector(Path tmpDir) {
    URL url = null;
    try {
      url = tmpDir.toUri().toURL();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    System.out.println(url);

    try {
      ClassLoader cl = new URLClassLoader(new URL[] {url});
      helperInjectorClass = cl.loadClass("io.opentelemetry.javaagent.tooling.HelperInjector");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Instruments OpenTelemetry javaagent with <code>OpenTelemetryAgentAdvices</code> and <code>
   * InstallBootstrapJarAdvice</code>
   *
   * @param jarFile File object representing the OpenTelemetry javaagent JAR file
   * @throws IOException If instrumentation with ByteBuddy throws exception
   */
  private void instrumentOpenTelemetryAgent() throws IOException {
    DynamicType.Unloaded<?> openTelemetryAgentType =
        new ByteBuddy()
            .rebase(openTelemetryAgentClass)
            .visit(
                Advice.to(OpenTelemetryAgentAdvices.class).on(isMethod().and(named("agentmain"))))
            .visit(
                Advice.to(InstallBootstrapJarAdvice.class)
                    .on(isMethod().and(named("installBootstrapJar"))))
            .make();
    classesToInstrument.put(
        openTelemetryAgentType.getTypeDescription().getInternalName() + ".class",
        openTelemetryAgentType.getBytes());
  }

  /**
   * Instruments OpenTelemetry javaagent file with <code>HelperInjectorAdvice</code> and inject
   * inject .classdata file (which is not possible through ByteBuddy injection) by repackaging
   * original JAR file to other file, and replacing the <code>HelperInjector.classdata</code> file
   * with the custom one
   *
   * @param jarFile File object representing the OpenTelemetry javaagent JAR file
   * @throws IOException If instrumentation with ByteBuddy throws exception
   */
  private void instrumentHelperInjector() throws IOException {
    DynamicType.Unloaded<?> helperInjectorType =
        new ByteBuddy()
            .rebase(helperInjectorClass)
            .visit(
                Advice.to(HelperInjectorAdvice.class)
                    .on(isMethod().and(named("injectBootstrapClassLoader"))))
            .make();

    classesToInstrument.put(
        "inst/" + helperInjectorType.getTypeDescription().getInternalName() + ".classdata",
        helperInjectorType.getBytes());
  }

  /**
   * Loads <code>BytesAndNames</code>, <code>PreTransformer</code>, <code>PostTransformer</code>
   * and' <code>StaticInstrumenter</code> classes
   */
  private void prepareAdditionalClasses() {
    List<Class<?>> classesToInject =
        Arrays.asList(
            BytesAndName.class,
            PreTransformer.class,
            PostTransformer.class,
            StaticInstrumenter.class);

    for (Class<?> clazz : classesToInject) {
      String[] clazzNameParts = clazz.getName().split("\\.");
      String clazzName = clazzNameParts[clazzNameParts.length - 1];

      String fullclazzName = "io.opentelemetry.javaagent." + clazzName;

      DynamicType.Unloaded<?> type = new ByteBuddy().rebase(clazz).name(fullclazzName).make();

      classesToInstrument.put(
          type.getTypeDescription().getInternalName() + ".class", type.getBytes());
    }
  }

  /**
   * Injects classes from <code>classesToInstrument</code> to <code>jarFile</code>
   *
   * @param jarFile File object representing the OpenTelemetry javaagent JAR file
   * @throws IOException If instrumentation with ByteBuddy throws exception
   */
  private void injectClasses(File jarFile) throws IOException {

    // following code mimics ByteBuddy's DynamicType.inject
    // we don't use it directly, since it would mean copying jar every time we add one class
    try (JarInputStream in = new JarInputStream(new FileInputStream(jarFile))) {

      Manifest manifest = in.getManifest();

      try (JarOutputStream zout =
          manifest == null
              ? new JarOutputStream(new FileOutputStream("tmp.jar"))
              : new JarOutputStream(new FileOutputStream("tmp.jar"), manifest)) {
        JarEntry entry;
        // find class that we want to replace
        while ((entry = in.getNextJarEntry()) != null) {
          byte[] replacement = classesToInstrument.remove(entry.getName());
          if (replacement == null) {
            zout.putNextEntry(entry);
            StaticInstrumenter.copy(in, zout);
          } else {
            zout.putNextEntry(new JarEntry(entry.getName()));
            zout.write(replacement);
            System.out.println("Instrumented " + entry.getName());
          }
          in.closeEntry();
          zout.closeEntry();
        }

        // now, add extra classes
        for (Map.Entry<String, byte[]> mapEntry : classesToInstrument.entrySet()) {
          zout.putNextEntry(new JarEntry(mapEntry.getKey()));
          zout.write(mapEntry.getValue());
          zout.closeEntry();

          System.out.println("Instrumented " + mapEntry.getKey());
        }
      }
    }

    Files.copy(Paths.get("tmp.jar"), jarFile.toPath(), REPLACE_EXISTING);
  }
}
