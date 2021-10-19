/* (C)2021 */
package agh.edu.pl.repackaging.config;

public class FolderNames {
  public static final String INSTRUMENTED_OTEL_JAR_PACKAGE_NAME = "INSTRUMENTED_OTEL_JAR";
  public static final String MAIN_JAR_INITIAL_COPY = "./MAIN_JAR_INITIAL_COPY";
  public static final String INSTRUMENTED_OTEL_JAR = "./" + INSTRUMENTED_OTEL_JAR_PACKAGE_NAME;
  public static final String INSTRUMENTED_DEPENDENCY = "./INSTRUMENTED_DEPENDENCY";
  public static final String JAR_WITH_INSTRUMENTED_DEPENDENCIES =
      "./JAR_WITH_INSTRUMENTED_DEPENDENCIES";
  public static final String INSTRUMENTED_JAR = "./INSTRUMENTED_JAR";
  public static final String OPENTELEMETRY_CLASSES = "./OPENTELEMETRY_CLASSES";
  public static final String FINAL = "./INSTRUMENTED_WITH_OPENTELEMETRY";
}
