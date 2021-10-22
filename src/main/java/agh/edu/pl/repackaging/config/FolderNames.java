/* (C)2021 */
package agh.edu.pl.repackaging.config;

import org.apache.commons.lang3.RandomStringUtils;

public class FolderNames {
  private static FolderNames instance;

  private String INSTRUMENTED_OTEL_JAR_PACKAGE_NAME;
  private final String MAIN_JAR_INITIAL_COPY;
  private final String INSTRUMENTED_DEPENDENCY;
  private final String JAR_WITH_INSTRUMENTED_DEPENDENCIES;
  private final String INSTRUMENTED_JAR;
  private final String OPENTELEMETRY_CLASSES;

  private FolderNames() {
    INSTRUMENTED_OTEL_JAR_PACKAGE_NAME =
        String.format("%s_%s", "INSTRUMENTED_OTEL_JAR", RandomStringUtils.randomAlphanumeric(8));
    INSTRUMENTED_OTEL_JAR_PACKAGE_NAME =
        String.format("%s_%s", "INSTRUMENTED_OTEL_JAR", RandomStringUtils.randomAlphanumeric(8));
    MAIN_JAR_INITIAL_COPY =
        String.format("%s_%s", "./MAIN_JAR_INITIAL_COPY", RandomStringUtils.randomAlphanumeric(8));
    INSTRUMENTED_DEPENDENCY =
        String.format(
            "%s_%s", "./INSTRUMENTED_DEPENDENCY", RandomStringUtils.randomAlphanumeric(8));
    JAR_WITH_INSTRUMENTED_DEPENDENCIES =
        String.format(
            "%s_%s",
            "./JAR_WITH_INSTRUMENTED_DEPENDENCIES", RandomStringUtils.randomAlphanumeric(8));
    INSTRUMENTED_JAR =
        String.format("%s_%s", "./INSTRUMENTED_JAR", RandomStringUtils.randomAlphanumeric(8));
    OPENTELEMETRY_CLASSES =
        String.format("%s_%s", "./OPENTELEMETRY_CLASSES", RandomStringUtils.randomAlphanumeric(8));
  }

  public static FolderNames getInstance() {
    if (instance == null) {
      instance = new FolderNames();
    }
    return instance;
  }

  public String getInstrumentedOtelJarPackageName() {
    return INSTRUMENTED_OTEL_JAR_PACKAGE_NAME;
  }

  public String getInstrumentedOtelJarPackage() {
    return String.format("%s/%s", ".", INSTRUMENTED_OTEL_JAR_PACKAGE_NAME);
  }

  public String getMainJARInitialCopyPackage() {
    return MAIN_JAR_INITIAL_COPY;
  }

  public String getInstrumentedDependencyPackage() {
    return INSTRUMENTED_DEPENDENCY;
  }

  public String getJARWithInstrumentedDependenciesPackage() {
    return JAR_WITH_INSTRUMENTED_DEPENDENCIES;
  }

  public String getInstrumentedJARPackage() {
    return INSTRUMENTED_JAR;
  }

  public String getOpenTelemetryClassesPackage() {
    return OPENTELEMETRY_CLASSES;
  }

  public String getFinalFolder() {
    return "./FINAL";
  }
}
