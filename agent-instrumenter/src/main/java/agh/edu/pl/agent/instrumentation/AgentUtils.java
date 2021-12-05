package agh.edu.pl.agent.instrumentation;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

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

  public static Path createAgentCopy(String agentPath, String pluginResourcesPath) {

    File agentFile = new File(agentPath);
    Path newAgentName = Paths.get(agentFile.getName());
    Path newAgentPath = Paths.get(pluginResourcesPath).resolve(newAgentName);

    try {
      return Files.copy(Paths.get(agentPath), newAgentPath, REPLACE_EXISTING);
    } catch (IOException e) {
      System.err.println("Could not copy agent from " + agentPath + " to " + newAgentPath);
      e.printStackTrace();
      System.exit(1);
      return null;
    }
  }

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
