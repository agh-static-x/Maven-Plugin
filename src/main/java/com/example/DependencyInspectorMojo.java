package com.example;

import com.example.instrumentation_poc.DependenciesGatherer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "dependency-inspection", defaultPhase = LifecyclePhase.PACKAGE)
public class DependencyInspectorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "agentPath", defaultValue = "opentelemetry-javaagent-static-all.jar")
    private String agentPath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        DependenciesGatherer gatherer = new DependenciesGatherer(project, agentPath);
        try {
            gatherer.instrumentDependencies();
            gatherer.instrumentMain();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

