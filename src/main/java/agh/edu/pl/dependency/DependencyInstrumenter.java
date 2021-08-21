package agh.edu.pl.dependency;

import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;

public class DependencyInstrumenter implements DependencyVisitor {

    @Override
    public boolean visitEnter(DependencyNode dependencyNode) {
        return true;
    }

    @Override
    public boolean visitLeave(DependencyNode dependencyNode) {
        System.out.println(dependencyNode.getArtifact().getFile());
        return true;
    }
}
