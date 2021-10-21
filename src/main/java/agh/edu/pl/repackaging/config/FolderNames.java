/* (C)2021 */
package agh.edu.pl.repackaging.config;

import org.apache.commons.lang3.RandomStringUtils;

public class FolderNames {
  private static FolderNames instance;

  private String INSTRUMENTED_OTEL_JAR_PACKAGE_NAME;
  private String MAIN_JAR_INITIAL_COPY;
  private String INSTRUMENTED_DEPENDENCY;
  private String JAR_WITH_INSTRUMENTED_DEPENDENCIES;
  private String INSTRUMENTED_JAR;
  private String OPENTELEMETRY_CLASSES;

  public static FolderNames getInstance() {
    if (instance == null) {
      instance = new FolderNames();
    }
    return instance;
  }

  public String getInstrumentedOtelJarPackageName() {
    if (INSTRUMENTED_OTEL_JAR_PACKAGE_NAME == null) {
      INSTRUMENTED_OTEL_JAR_PACKAGE_NAME =
          String.format("%s%s", "INSTRUMENTED_OTEL_JAR_", RandomStringUtils.randomAlphanumeric(8));
    }
    return INSTRUMENTED_OTEL_JAR_PACKAGE_NAME;
  }

  public String getInstrumentedOtelJarPackage() {
    if (INSTRUMENTED_OTEL_JAR_PACKAGE_NAME == null) {
      INSTRUMENTED_OTEL_JAR_PACKAGE_NAME =
          String.format("%s%s", "INSTRUMENTED_OTEL_JAR_", RandomStringUtils.randomAlphanumeric(8));
    }
    return String.format("%s/%s", ".", INSTRUMENTED_OTEL_JAR_PACKAGE_NAME);
  }

  public String getMainJARInitialCopyPackage() {
    if (MAIN_JAR_INITIAL_COPY == null) {
      MAIN_JAR_INITIAL_COPY =
          String.format(
              "%s%s", "./MAIN_JAR_INITIAL_COPY_", RandomStringUtils.randomAlphanumeric(8));
    }
    return MAIN_JAR_INITIAL_COPY;
  }

  public String getInstrumentedDependencyPackage() {
    if (INSTRUMENTED_DEPENDENCY == null) {
      INSTRUMENTED_DEPENDENCY =
          String.format(
              "%s%s", "./INSTRUMENTED_DEPENDENCY_", RandomStringUtils.randomAlphanumeric(8));
    }
    return INSTRUMENTED_DEPENDENCY;
  }

  public String getJARWithInstrumentedDependenciesPackage() {
    if (JAR_WITH_INSTRUMENTED_DEPENDENCIES == null) {
      JAR_WITH_INSTRUMENTED_DEPENDENCIES =
          String.format(
              "%s%s",
              "./JAR_WITH_INSTRUMENTED_DEPENDENCIES_", RandomStringUtils.randomAlphanumeric(8));
    }
    return JAR_WITH_INSTRUMENTED_DEPENDENCIES;
  }

  public String getInstrumentedJARPackage() {
    if (INSTRUMENTED_JAR == null) {
      INSTRUMENTED_JAR =
          String.format("%s%s", "./INSTRUMENTED_JAR_", RandomStringUtils.randomAlphanumeric(8));
    }
    return INSTRUMENTED_JAR;
  }

  public String getOpenTelemetryClassesPackage() {
    if (OPENTELEMETRY_CLASSES == null) {
      OPENTELEMETRY_CLASSES =
          String.format(
              "%s%s", "./OPENTELEMETRY_CLASSES_", RandomStringUtils.randomAlphanumeric(8));
    }
    return OPENTELEMETRY_CLASSES;
  }

  public String getFinalFolder() {
    return "./FINAL";
  }
}
