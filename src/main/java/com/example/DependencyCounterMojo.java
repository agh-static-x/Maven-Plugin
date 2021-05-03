package com.example;

import com.example.dependency_tree.DependencyTree;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mojo(name = "dependency", defaultPhase = LifecyclePhase.COMPILE)
public class DependencyCounterMojo extends AbstractMojo {

    /**
     * The list used to put files
     */
    private static final List<String> fileList = new ArrayList<>();

    /**
     * The number of all the lines in the project.
     */
    private int allLines = 0;

    /**
     * The folder where the project that introduced this plugin is passed in.
     */
    @Parameter(property = "currentBaseDir", defaultValue = "User/pathHome")
    private String currentBaseDir;

    /**
     *  The file type that the project that introduced this plug-in comes in (eg. java).
     */
    @Parameter(property = "suffix", defaultValue = ".java")
    private String suffix;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
/*        List<String> fileList = scanFile(currentBaseDir);
        System.out.println("FilePath:" + currentBaseDir);
        System.out.println("FileSuffix:" + suffix);
        System.out.println("FileTotal:" + fileList.size());
        System.out.println("allLines:" + allLines);*/

        DependencyTree dependencyTree = new DependencyTree(project);
        dependencyTree.showDependencyTree();
    }

    /**
     * Recursively collects file statistics.
     * Puts all files that meet the conditions into the collection.
     */
    private List<String> scanFile(String filePath) {
        File dir = new File(filePath);
        for(File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                scanFile(file.getAbsolutePath());
            } else {
                if (file.getName().endsWith(suffix)) {
                    fileList.add(file.getName());
                    allLines += countLines(file);
                }
            }
        }
        return fileList;
    }

    /**
     * Counts the number of lines in a file.
     *
     * @param file  the file
     * @return the number of lines in the file
     */
    private int countLines(File file) {
        int lines = 0;
        try {
            BufferedReader reader =  new BufferedReader(new FileReader(file));
            while (reader.ready()) {
                reader.readLine();
                lines++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}

