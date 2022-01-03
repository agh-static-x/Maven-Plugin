/* (C)2021 */
package agh.edu.pl.repackaging.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemporaryFolders {
  private static final Logger logger = LoggerFactory.getLogger(TemporaryFolders.class);

  private static TemporaryFolders instance;
  private final Path INSTRUMENTED_OTEL_JAR_PACKAGE_NAME;
  private final Path MAIN_JAR_INITIAL_COPY;
  private final Path JAR_WITH_INSTRUMENTED_DEPENDENCIES;
  private final Path INSTRUMENTED_JAR;
  private final Path OPENTELEMETRY_CLASSES;
  private final Path FRAMEWORK_SUPPORT_FOLDER;
  private String FINAL_FOLDER;

  private TemporaryFolders() throws IOException {
    INSTRUMENTED_OTEL_JAR_PACKAGE_NAME =
        Files.createTempDirectory("INSTRUMENTED_OTEL_JAR_PACKAGE_NAME");
    MAIN_JAR_INITIAL_COPY = Files.createTempDirectory("MAIN_JAR_INITIAL_COPY");
    JAR_WITH_INSTRUMENTED_DEPENDENCIES =
        Files.createTempDirectory("JAR_WITH_INSTRUMENTED_DEPENDENCIES");
    INSTRUMENTED_JAR = Files.createTempDirectory("INSTRUMENTED_JAR");
    OPENTELEMETRY_CLASSES = Files.createTempDirectory("OPENTELEMETRY_CLASSES");
    FRAMEWORK_SUPPORT_FOLDER = Files.createTempDirectory("FRAMEWORK_SUPPORT_FOLDER");
  }

  public static void create() throws IOException {
    try {
      instance = new TemporaryFolders();
    } catch (IOException e) {
      logger.error("Could not create required temporary folders");
      throw e;
    }
  }

  public static void delete() throws IOException {
    try {
      FileUtils.deleteDirectory(instance.INSTRUMENTED_OTEL_JAR_PACKAGE_NAME.toFile());
      FileUtils.deleteDirectory(instance.MAIN_JAR_INITIAL_COPY.toFile());
      FileUtils.deleteDirectory(instance.JAR_WITH_INSTRUMENTED_DEPENDENCIES.toFile());
      FileUtils.deleteDirectory(instance.INSTRUMENTED_JAR.toFile());
      FileUtils.deleteDirectory(instance.OPENTELEMETRY_CLASSES.toFile());
      FileUtils.deleteDirectory(instance.FRAMEWORK_SUPPORT_FOLDER.toFile());
      instance = null;
    } catch (IOException e) {
      logger.error("Could not delete created temporary folders");
      throw e;
    }
  }

  public static TemporaryFolders getInstance() {
    return instance;
  }

  public Path getInstrumentedOtelJarPackage() {
    return INSTRUMENTED_OTEL_JAR_PACKAGE_NAME;
  }

  public Path getMainJARInitialCopyPackage() {
    return MAIN_JAR_INITIAL_COPY;
  }

  public Path getJARWithInstrumentedDependenciesPackage() {
    return JAR_WITH_INSTRUMENTED_DEPENDENCIES;
  }

  public Path getInstrumentedJARPackage() {
    return INSTRUMENTED_JAR;
  }

  public Path getOpenTelemetryClassesPackage() {
    return OPENTELEMETRY_CLASSES;
  }

  public Path getFrameworkSupportFolder() {
    return FRAMEWORK_SUPPORT_FOLDER;
  }

  public String getFinalFolder() {
    return FINAL_FOLDER;
  }

  public void setFinalFolder(String name) {
    this.FINAL_FOLDER = name;
  }
}
