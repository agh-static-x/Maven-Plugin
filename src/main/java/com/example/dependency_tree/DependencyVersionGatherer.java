package com.example.dependency_tree;

import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyVersionGatherer implements DependencyVisitor {

    Map<String, List<String>> dependencies = new HashMap<>();

    @Override
    public boolean visitEnter(DependencyNode dependencyNode) {
        String id = dependencyNode.getArtifact().getGroupId() + ":" +dependencyNode.getArtifact().getArtifactId();
        if (dependencies.containsKey(id)) {
            List<String> versions = dependencies.get(id);
            if(!versions.contains(dependencyNode.getArtifact().getVersion())) {
                versions.add(dependencyNode.getArtifact().getVersion());
            }
            dependencies.put(id,versions);
        }
        else {
            List<String> versions = new ArrayList<>();
            versions.add(dependencyNode.getArtifact().getVersion());
            dependencies.put(id, versions);
        }
        return true;
    }

    @Override
    public boolean visitLeave(DependencyNode dependencyNode) {
        return true;
    }

    public void printDependencies(){
        System.out.println("DEPENDENCIES VERSIONS");
        for(Map.Entry<String, List<String>> entry : dependencies.entrySet()){
            System.out.print(entry.getKey()+", versions: ");
            for(String version:entry.getValue()){
                System.out.print(version+", ");
            }
            System.out.print("\n");
        }
    }
}
