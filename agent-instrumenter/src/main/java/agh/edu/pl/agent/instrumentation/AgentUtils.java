/*
 * SPDX-License-Identifier: Apache-2.0
 */
package agh.edu.pl.agent.instrumentation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/** Provides utility methods for javaagent instrumentation */
public class AgentUtils {

  private final Path tmpDir;
  private final Path extractedAgent;

  public AgentUtils() throws IOException {
    tmpDir = Files.createTempDirectory("tmp-class-dir");
    extractedAgent = Files.createDirectories(tmpDir.resolve("opentelemetry-javaagent"));

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  try {
                    FileUtils.deleteDirectory(tmpDir.toFile());
                  } catch (IOException ex) {
                    ex.printStackTrace();
                  }
                }));
  }

  /**
   * Copies the javaagent file to the plugin <code>resources</code> folder
   *
   * @param agentPath Path to javaagent file that will be copied
   * @param pluginResourcesPath Path to resources folder in plugin module
   * @return Path to the copied javaagent file
   */
  public Path extractAgent(File agentFile) throws IOException {
    JarFile otelJarFile = new JarFile(agentFile);

    Enumeration<JarEntry> entries = otelJarFile.entries();

    while (entries.hasMoreElements()) {

      JarEntry entry = entries.nextElement();
      String name = entry.getName();

      String sanitizedName;

      if (name.contains("inst/") && name.contains(".classdata")) {
        sanitizedName = name.replace(".classdata", "").replace("inst/", "");
      } else {
        sanitizedName = name.replace(".class", "");
      }

      extractEntry(otelJarFile, entry, sanitizedName);
    }

    return extractedAgent;
  }

  /**
   * Extracts the JAR entry to temporary directory under new, sanitized name
   *
   * @param otelJarFile JarFile object representing the OpenTelemetry javaagent file
   * @param entryToSave JAR entry that is extracted
   * @param sanitized Sanitized JAR entry name
   * @throws IOException If process of writing to file encounters problems
   */
  private void extractEntry(JarFile otelJarFile, JarEntry entryToSave, String sanitized)
      throws IOException {
    int lastSlashIdx = sanitized.lastIndexOf("/");
    String path =
        sanitized.substring(0, lastSlashIdx).replace("/", System.getProperty("file.separator"));
    String className = sanitized.substring(lastSlashIdx + 1) + ".class";

    Path classPackage = Files.createDirectories(extractedAgent.resolve(path));

    Files.write(
        classPackage.resolve(className),
        IOUtils.toByteArray(otelJarFile.getInputStream(entryToSave)));
  }
}
