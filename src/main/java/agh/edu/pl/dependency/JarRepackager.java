/* (C)2021 */
package agh.edu.pl.dependency;

import agh.edu.pl.dependency.JarRepackageClasses.OpenTelemetryClasses;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

public class JarRepackager {
  private final MavenProject project;
  String TMP_DIR = "./TMP";
  public static final String OTEL_TMP = "./INSTRUMENTED_OTEL";
  String INSTRUMENTED_FILE_DIR = "./INSTRUMENTED_FILE";
  String INSTRUMENTED = "./INSTRUMENTED_JAR";
  String FINAL = "./INSTRUMENTED_FINAL";
  private final String agentPath;
  private File jarFile;

  private final String STATIC_INSTRUMENTER_CLASS = "io.opentelemetry.javaagent.StaticInstrumenter";

  public JarRepackager(MavenProject project, String agentPath) {
    this.project = project;
    this.agentPath = OTEL_TMP + "/" + agentPath;
  }

  public void listArtifacts() {
    ArrayList<File> artifacts = new ArrayList<>();
    artifacts.add(project.getArtifact().getFile());
    List<Artifact> attachedArtifacts = project.getAttachedArtifacts();
    if (attachedArtifacts.isEmpty()) {
      this.jarFile = artifacts.get(0);
      return;
    } else {
      attachedArtifacts.forEach((Artifact artifact) -> artifacts.add(artifact.getFile()));
    }
    System.out.println("Choose the JAR file you want to add telemetry to:");
    for (int i = 0; i < artifacts.size(); i++) {
      System.out.println(i + " - " + artifacts.get(i).getName());
    }
    Scanner scanner = new Scanner(System.in);
    int number = scanner.nextInt();
    while (number < 0 || number > artifacts.size() - 1) {
      System.out.println("Number is out of scope. Please enter another number.");
      number = scanner.nextInt();
    }
    this.jarFile = artifacts.get(number);
  }

  public void instrumentMain() throws Exception {
    try {
      JarFile jarFile = null;
      try {
        jarFile = new JarFile(this.jarFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
      String pattern = Pattern.quote(System.getProperty("file.separator"));
      if (jarFile == null) {
        System.err.println("No JAR for project found.");
        return;
      }
      String[] outFileNameParts = jarFile.getName().split(pattern);
      final String outFileName = INSTRUMENTED + "/" + outFileNameParts[outFileNameParts.length - 1];
      String mainPath = outFileName + File.pathSeparator;
      Process process =
          new ProcessBuilder(
                  "java",
                  "-Dota.static.instrumenter=true",
                  "-Dotel.javaagent.experimental.field-injection.enabled=false",
                  String.format("-javaagent:%s", agentPath),
                  "-cp",
                  String.format("%s", mainPath),
                  STATIC_INSTRUMENTER_CLASS,
                  FINAL)
              .inheritIO()
              .start();
      int ret = process.waitFor();
    } finally {
      FileUtils.deleteDirectory(OTEL_TMP);
      FileUtils.deleteDirectory(INSTRUMENTED);
    }
  }

  public void instrumentDependencies() throws Exception {
    try {
      File tmpDir = new File(TMP_DIR);
      tmpDir.mkdir();
      File instrumentedDir = new File(INSTRUMENTED);
      instrumentedDir.mkdir();
      File instrumentedFileDirectory = new File(INSTRUMENTED_FILE_DIR);
      instrumentedFileDirectory.mkdir();

      JarFile jarFile = null;
      try {
        jarFile = new JarFile(this.jarFile);
      } catch (IOException e) {
        e.printStackTrace();
      }

      if (jarFile == null) {
        System.err.println("Error while getting project jar file.");
        return;
      }
      String pattern = Pattern.quote(System.getProperty("file.separator"));
      String[] outFileNameParts = jarFile.getName().split(pattern);
      final String outFileName = outFileNameParts[outFileNameParts.length - 1];
      final File outFile = new File(INSTRUMENTED, outFileName);
      final ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(outFile));
      zout.setMethod(ZipOutputStream.STORED);

      for (Enumeration<JarEntry> enums = jarFile.entries(); enums.hasMoreElements(); ) {
        JarEntry entry = enums.nextElement();
        if (entry.getName().endsWith(".jar")) {
          StringBuilder classpath = new StringBuilder();
          String fileName = TMP_DIR + "/" + entry.getName();
          File f = new File(fileName);
          f.getParentFile().mkdirs();
          Files.copy(jarFile.getInputStream(entry), f.toPath());
          classpath.append(fileName).append(File.pathSeparator);
          System.out.println(classpath);
          Process process =
              new ProcessBuilder(
                      "java",
                      "-Dota.static.instrumenter=true",
                      "-Dotel.javaagent.experimental.field-injection.enabled=false",
                      String.format("-javaagent:%s", agentPath),
                      "-cp",
                      String.format("%s", classpath),
                      STATIC_INSTRUMENTER_CLASS,
                      INSTRUMENTED_FILE_DIR)
                  .inheritIO()
                  .start();
          int ret = process.waitFor();
          String[] fileNameParts = entry.getName().split("/");
          createZipEntry(
              zout,
              new File(INSTRUMENTED_FILE_DIR + "/" + fileNameParts[fileNameParts.length - 1]),
              entry.getName());
          FileUtils.cleanDirectory(TMP_DIR);
          FileUtils.cleanDirectory(INSTRUMENTED_FILE_DIR);
        } else {
          ZipEntry outEntry = new ZipEntry(entry);
          zout.putNextEntry(outEntry);
          InputStream in = jarFile.getInputStream(entry);
          in.transferTo(zout);
          in.close();
          zout.closeEntry();
        }
      }
      zout.close();
      jarFile.close();
    } finally {
      FileUtils.deleteDirectory(TMP_DIR);
      FileUtils.deleteDirectory(INSTRUMENTED_FILE_DIR);
    }
  }

  public void addOpenTelemetryClasses() {
    String pattern = Pattern.quote(System.getProperty("file.separator"));
    String[] outFileNameParts = jarFile.getName().split(pattern);
    final String outFileName = FINAL + "/" + outFileNameParts[outFileNameParts.length - 1];
    OpenTelemetryClasses openTelemetryClasses =
        new OpenTelemetryClasses(new File(outFileName), agentPath, FINAL);
    try {
      openTelemetryClasses.addOpenTelemetryFolders();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void createZipEntry(ZipOutputStream zout, File file, String pathToFile)
      throws IOException {
    createZipEntryFromFile(zout, file, pathToFile);
  }

  public static void createZipEntryFromFile(ZipOutputStream zout, File file, String pathToFile)
      throws IOException {
    InputStream is = new FileInputStream(file);
    ZipEntry entry = new ZipEntry(pathToFile);
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
