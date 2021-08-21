package agh.edu.pl;

import agh.edu.pl.dependency.DependenciesGatherer;
import agh.edu.pl.agent.instrumentation.OpenTelemetryLoader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;

@Mojo(name = "dependency-inspection", defaultPhase = LifecyclePhase.PACKAGE)
public class DependencyInspectorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "agentPath", defaultValue = "opentelemetry-javaagent-all.jar")
    private String agentPath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        OpenTelemetryLoader loader = new OpenTelemetryLoader(agentPath);
        try {
            loader.instrument();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        DependenciesGatherer gatherer = new DependenciesGatherer(project, agentPath);
        try {
            gatherer.instrumentDependencies();
            gatherer.instrumentMain();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

