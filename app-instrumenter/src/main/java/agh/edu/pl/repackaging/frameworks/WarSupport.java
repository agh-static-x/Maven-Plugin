package agh.edu.pl.repackaging.frameworks;

import java.util.HashSet;

public class WarSupport implements FrameworkSupport{
    private final String prefix = "WEB-INF/classes/";
    private final HashSet<String> filesToRepackage = new HashSet<>();

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public void addFileToRepackage(String fileName) {
        filesToRepackage.add(fileName);
    }

    @Override
    public HashSet<String> getFilesToRepackage() {
        return filesToRepackage;
    }
}
