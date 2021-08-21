/* (C)2021 */
package agh.edu.pl.agent.instrumentation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;

public class JarUtils {

  public static File copyFile(String filePath, String destination) {
    try {
      File tmpDir = new File(destination);
      tmpDir.mkdir();

      String[] pathArr = filePath.split(System.getProperty("file.separator"));
      String fileName = pathArr[pathArr.length - 1];

      File copyFile = new File(destination + System.getProperty("file.separator") + fileName);
      Files.copy(new File(fileName).toPath(), copyFile.toPath());
      System.out.println("Copied " + fileName + " to " + destination);
      return copyFile;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static synchronized Class<?> loadClassFromJar(String jarPath, String className) {
    try {
      File jarFile = new File(jarPath);
      URLClassLoader classLoader =
          new URLClassLoader(
              new URL[] {jarFile.toURI().toURL()}, OpenTelemetryLoader.class.getClassLoader());

      Class<?> openTelemetryAgentClass = Class.forName(className, true, classLoader);
      System.out.println("Loaded OpenTelemetryAgent: " + openTelemetryAgentClass);
      return openTelemetryAgentClass;
    } catch (ClassNotFoundException | IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
