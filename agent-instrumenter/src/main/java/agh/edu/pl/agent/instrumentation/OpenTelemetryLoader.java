/* (C)2021 */
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
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;

/** Responsible for loading and instrumenting the OpenTelemetry javaagent JAR file */
public class OpenTelemetryLoader {
  public URLClassLoader otelClassLoader;
  private Class<?> openTelemetryAgentClass;
  private Class<?> helperInjectorClass;
  public String otelJarPath;
  private final String pluginResourcesPath;

  public OpenTelemetryLoader(String otelJarPath, String pluginResourcesPath) {
    this.otelJarPath = otelJarPath;
    this.pluginResourcesPath = pluginResourcesPath;
  }

  /** Conducts the process of OpenTelemetry javaagent JAR instrumentation */
  public void instrument() {
    loadClasses(new File(otelJarPath));
    File otelJarFile = new File(otelJarPath);
    File copyFile = AgentUtils.createAgentCopy(otelJarPath, pluginResourcesPath).toFile();

    try {
      Files.copy(otelJarFile.toPath(), copyFile.toPath(), REPLACE_EXISTING);
    } catch (IOException exception) {
      System.err.println(
          "OpenTelemetry Agent JAR could not be copied to instrumentation directory.");
      return;
    }
    System.out.println("Copied OTEL to " + pluginResourcesPath);
    try {
      instrumentOpenTelemetryAgent(copyFile);
      instrumentHelperInjector(copyFile);
    } catch (IOException exception) {
      exception.printStackTrace();
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

  /**
   * Loads <code>io.opentelemetry.javaagent.OpenTelemetryAgent</code> class from OpenTelemetry JAR
   * file
   *
   * @param otelJar File object representing the OpenTelemetry javvagent JAR file
   */
  public synchronized void loadClasses(File otelJar) {
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
  private void instrumentOpenTelemetryAgent(File jarFile) throws IOException {
    new ByteBuddy()
        .rebase(openTelemetryAgentClass)
        .visit(Advice.to(OpenTelemetryAgentAdvices.class).on(isMethod().and(named("agentmain"))))
        .visit(
            Advice.to(InstallBootstrapJarAdvice.class)
                .on(isMethod().and(named("installBootstrapJar"))))
        .make()
        .inject(jarFile);
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
  private void instrumentHelperInjector(File jarFile) throws IOException {
    byte[] helperInjectorBytes =
        new ByteBuddy()
            .rebase(helperInjectorClass)
            .visit(
                Advice.to(HelperInjectorAdvice.class)
                    .on(isMethod().and(named("injectBootstrapClassLoader"))))
            .make()
            .getBytes();

    ZipInputStream in = new ZipInputStream(new FileInputStream(jarFile));
    ZipOutputStream zout = new ZipOutputStream(new FileOutputStream("tmp.jar"));

    ZipEntry entry;
    while ((entry = in.getNextEntry()) != null) {
      if (entry
          .getName()
          .equals("inst/io/opentelemetry/javaagent/tooling/HelperInjector.classdata")) continue;
      zout.putNextEntry(entry);
      StaticInstrumenter.copy(in, zout);
      zout.closeEntry();
    }

    ZipEntry newEntry =
        new ZipEntry("inst/io/opentelemetry/javaagent/tooling/HelperInjector.classdata");
    newEntry.setSize(helperInjectorBytes.length);
    newEntry.setCompressedSize(-1);
    zout.putNextEntry(newEntry);

    StaticInstrumenter.copy(new ByteArrayInputStream(helperInjectorBytes), zout);
    zout.closeEntry();

    zout.close();
    in.close();

    Files.copy(Paths.get("tmp.jar"), jarFile.toPath(), REPLACE_EXISTING);
  }

  /**
   * Injects <code>BytesAndNames</code>, <code>PreTransformer</code>, <code>PostTransformer</code>
   * and' <code>StaticInstrumenter</code> classes into OpenTelemetry javaagent file
   *
   * @param jarFile File object representing the OpenTelemetry javaagent JAR file
   * @throws IOException If instrumentation with ByteBuddy throws exception
   */
  public void injectClasses(File jarFile) throws IOException {
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

      new ByteBuddy().rebase(clazz).name(fullclazzName).make().inject(jarFile);
      System.out.println("Instrumented " + fullclazzName);
    }
  }
}
