package agh.edu.pl.logger;

import java.util.Locale;

public final class LoggingConfigurer {

  private static final String SIMPLE_LOGGER_SHOW_DATE_TIME_PROPERTY =
      "io.opentelemetry.javaagent.slf4j.simpleLogger.showDateTime";
  private static final String SIMPLE_LOGGER_DATE_TIME_FORMAT_PROPERTY =
      "io.opentelemetry.javaagent.slf4j.simpleLogger.dateTimeFormat";
  private static final String SIMPLE_LOGGER_DATE_TIME_FORMAT_DEFAULT =
      "'[otel.javaagent 'yyyy-MM-dd HH:mm:ss:SSS Z']'";
  private static final String SIMPLE_LOGGER_DEFAULT_LOG_LEVEL_PROPERTY =
      "io.opentelemetry.javaagent.slf4j.simpleLogger.defaultLogLevel";
  private static final String SIMPLE_LOGGER_PREFIX =
      "io.opentelemetry.javaagent.slf4j.simpleLogger.log.";

  public static void configureLogger() {
    setSystemPropertyDefault(SIMPLE_LOGGER_SHOW_DATE_TIME_PROPERTY, "true");
    setSystemPropertyDefault(
        SIMPLE_LOGGER_DATE_TIME_FORMAT_PROPERTY, SIMPLE_LOGGER_DATE_TIME_FORMAT_DEFAULT);

    if (isDebugMode()) {
      setSystemPropertyDefault(SIMPLE_LOGGER_DEFAULT_LOG_LEVEL_PROPERTY, "DEBUG");
      setSystemPropertyDefault(SIMPLE_LOGGER_PREFIX + "okhttp3.internal.http2", "INFO");
    } else {
      // by default muzzle warnings are turned off
      setSystemPropertyDefault(SIMPLE_LOGGER_PREFIX + "muzzleMatcher", "OFF");
    }
  }

  private static void setSystemPropertyDefault(String property, String value) {
    if (System.getProperty(property) == null) {
      System.setProperty(property, value);
    }
  }

  /**
   * Determine if we should log in debug level according to otel.javaagent.debug
   *
   * @return true if we should
   */
  private static boolean isDebugMode() {
    String tracerDebugLevelSysprop = "otel.javaagent.debug";
    String tracerDebugLevelProp = System.getProperty(tracerDebugLevelSysprop);

    if (tracerDebugLevelProp != null) {
      return Boolean.parseBoolean(tracerDebugLevelProp);
    }

    String tracerDebugLevelEnv =
        System.getenv(tracerDebugLevelSysprop.replace('.', '_').toUpperCase(Locale.ROOT));

    if (tracerDebugLevelEnv != null) {
      return Boolean.parseBoolean(tracerDebugLevelEnv);
    }
    return false;
  }

  private LoggingConfigurer() {}
}
