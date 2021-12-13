package agh.edu.pl.repackaging.config;

/**
 * Contains parameters required for instrumentation process.
 */
public class InstrumentationConfiguration {
  private String classpath;
  private String transitiveDependencies;

  public String getClasspath() {
    return classpath;
  }

  public String getTransitiveDependencies() {
    return transitiveDependencies;
  }

  public void setClasspath(String classpath) {
    this.classpath = classpath;
  }

  public void setTransitiveDependencies(String transitiveDependencies) {
    this.transitiveDependencies = transitiveDependencies;
  }
}
