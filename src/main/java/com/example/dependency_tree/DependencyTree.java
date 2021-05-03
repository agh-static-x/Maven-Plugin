package com.example.dependency_tree;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class DependencyTree {
    private final MavenProject project;

    public DependencyTree(MavenProject project){
        this.project= project;
    }

    public void showDependencyTree(){
        Set<Artifact> dependencies = project.getDependencyArtifacts();

        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();

        locator.addService( RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class );
        locator.addService( TransporterFactory.class, FileTransporterFactory.class );
        locator.addService( TransporterFactory.class, HttpTransporterFactory.class );

        RepositorySystem repositorySystem = locator.getService( RepositorySystem.class );
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepo = new LocalRepository( "target/local-repo" );
        session.setLocalRepositoryManager( repositorySystem.newLocalRepositoryManager( session, localRepo ) );

        CollectRequest collectRequest = new CollectRequest();

        collectRequest.setRepositories(new ArrayList<>( Collections.singletonList(
                new RemoteRepository.Builder( "central", "default", "https://repo.maven.apache.org/maven2/"  ).build())));

        for (Artifact art : dependencies) {
            collectRequest.setRoot(new Dependency(new DefaultArtifact(StringUtils.substringBeforeLast(art.toString(), ":")), ""));
            try {
                System.out.println("--------------------------------------------");
                CollectResult collectResult = repositorySystem.collectDependencies(session, collectRequest);
                collectResult.getRoot().accept(new ConsoleDependencyGraphDumper());
                System.out.println("--------------------------------------------");
            } catch (DependencyCollectionException e) {
                e.printStackTrace();
            }
        }
    }
}
