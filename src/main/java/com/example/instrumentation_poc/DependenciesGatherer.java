package com.example.instrumentation_poc;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DependenciesGatherer {
    private final MavenProject project;
    String TMP_DIR = "./tmp";
    String LIB_DIR = "./tmp/BOOT-INF/lib";
    String INSTRUMENTED = "./instrumented";
    private final String agentPath;

    public DependenciesGatherer(MavenProject project, String agentPath){
        this.project = project;
        this.agentPath = agentPath;
    }

    public void instrumentMain() throws Exception {
        File mainJar = project.getArtifact().getFile();
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(mainJar);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] outFileNameParts = jarFile.getName().split("\\\\");
        final String outFileName = outFileNameParts[outFileNameParts.length - 1];
        String mainPath = outFileName + File.pathSeparator + agentPath;
        Process process = new ProcessBuilder("java", "-Dota.static.instrumenter=true",
                String.format("-javaagent:%s", agentPath), "-cp", String.format("%s", mainPath),
                "io.opentelemetry.javaagent.bootstrap.StaticInstrumenter",
                INSTRUMENTED).inheritIO().start();
        int ret = process.waitFor();
    }

    public void instrumentDependencies() throws Exception {
        try {
            /*Set<Artifact> dependencies = project.getDependencyArtifacts();*/
            File tmpDir = new File(TMP_DIR);
            tmpDir.mkdir();
            File tmpBoot = new File(TMP_DIR + "/BOOT-INF");
            tmpBoot.mkdir();
            File tmpLib = new File(TMP_DIR + "/BOOT-INF/lib");
            tmpLib.mkdir();
            File instrumentedDir = new File(INSTRUMENTED);
            instrumentedDir.mkdir();

            File mainJar = project.getArtifact().getFile();
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(mainJar);
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert jarFile != null;
            String[] outFileNameParts = jarFile.getName().split("\\\\");
            final String outFileName = outFileNameParts[outFileNameParts.length - 1];
            //final File outFile = new File(INSTRUMENTED, outFileName);
            final File outFile = new File(outFileName);
            final ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(outFile));

            String COMMAND = "java -Dota.static.instrumenter=true "
                    + String.format("-javaagent:%s", agentPath)
                    + " -cp %to_instrument% io.opentelemetry.javaagent.bootstrap.StaticInstrumenter %dir%";

            StringBuilder libFiles = new StringBuilder();
            for (Enumeration<JarEntry> enums = jarFile.entries(); enums.hasMoreElements(); ) {
                JarEntry entry = enums.nextElement();
                if (entry.getName().endsWith(".jar")) {
                    String[] nameParts = entry.getName().split("/");
                    String fileName = TMP_DIR + "/" + nameParts[nameParts.length - 1];
                    File f = new File(fileName);
                    Files.copy(jarFile.getInputStream(entry), f.toPath());
                    libFiles.append(fileName).append(File.pathSeparator);
                } else {
                    ZipEntry outEntry = new ZipEntry(entry);
                    zout.putNextEntry(outEntry);
                    InputStream in = jarFile.getInputStream(entry);
                    in.transferTo(zout);
                    in.close();
                    zout.closeEntry();
                }
            }

            libFiles.append(agentPath).append(File.pathSeparator);

            Process process = new ProcessBuilder("java", "-Dota.static.instrumenter=true",
                    String.format("-javaagent:%s", agentPath), "-cp", String.format("%s", libFiles),
                    "io.opentelemetry.javaagent.bootstrap.StaticInstrumenter",
                    LIB_DIR).inheritIO().start();
            int ret = process.waitFor();

            zout.setMethod(ZipOutputStream.STORED);

            for (final File jar : Objects.requireNonNull(tmpLib.listFiles())) {
                createZipEntry(zout, jar);
            }

            zout.close();
            jarFile.close();
        } finally {
            FileUtils.deleteDirectory(TMP_DIR);
        }
    }

    private void createZipEntry(ZipOutputStream zout, File file) throws IOException {
        InputStream is = new FileInputStream(file);
        String[] nameParts = file.getName().split("/");
        ZipEntry entry = new ZipEntry("BOOT-INF/lib/" + nameParts[nameParts.length - 1]);
        byte[] bytes = Files.readAllBytes(file.toPath());
        entry.setCompressedSize(file.length());
        entry.setSize(file.length());
        CRC32 crc = new CRC32();
        crc.update(bytes);
        entry.setCrc(crc.getValue());
        zout.putNextEntry(entry);
        is.transferTo(zout);
        is.close();
        zout.closeEntry();
    }

    private static void copy(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buf = new byte[1024];
        int read = in.read(buf);
        while (read != -1) {
            out.write(buf, 0, read);
            read = in.read(buf);
        }
    }
}