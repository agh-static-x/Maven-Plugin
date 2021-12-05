/* (C)2021 */
package io.opentelemetry.javaagent;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticInstrumenter {
  // FIXME classloaders in use by loaded jars, classloader as key
  // key is slashy name, not dotty
  public static final Map<String, byte[]> InstrumentedClasses = new ConcurrentHashMap<>();

  public static final ThreadLocal<BytesAndName> CurrentClass = new ThreadLocal<>();

  private static final Logger logger = LoggerFactory.getLogger(StaticInstrumenter.class);

  public static ClassFileTransformer getPreTransformer() {
    return new PreTransformer();
  }

  public static ClassFileTransformer getPostTransformer() {
    return new PostTransformer();
  }

  // Above happens in -javaagent-space, below in main-space
  public static void main(final String[] args) throws Exception {

    logger.debug("[CLASSPATH] " + System.getProperty("java.class.path"));

    // FIXME error handling, user niceties, etc.
    final File outDir = new File(args[0]);
    if (!outDir.exists()) {
      outDir.mkdir();
    }

    String[] transitiveDependencies = {};
    if (args.length > 1) {
      transitiveDependencies = args[1].split(System.getProperty("path.separator"));
    }

    for (final String pathItem :
        System.getProperty("java.class.path").split(System.getProperty("path.separator"))) {
      logger.debug("[PATH_ITEM] " + pathItem);
      // FIXME java 9 / jmod support, proper handling of directories, just generally better and more
      // resilient stuff
      // FIXME jmod in particular introduces weirdness with adding helpers to the dependencies
      if (pathItem.endsWith(".jar") || pathItem.endsWith(".war")) {
        processJar(
            new File(pathItem), outDir, Arrays.asList(transitiveDependencies).contains(pathItem));
      }
    }
  }

  private static boolean shouldSkip(final String entryName) {
    return entryName.startsWith("io/opentelemetry");
  }

  private static void processJar(final File jar, final File outDir, final boolean isTransitive)
      throws Exception {
    //    System.out.println("[processJar] " + jar.getName());
    // FIXME don't "instrument" our agent jar.
    final File outFile = new File(outDir, jar.getName()); // FIXME multiple jars with same name
    // FIXME detect and warn on signed jars (and drop the signing bits)

    final JarFile in = new JarFile(jar);
    final ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(outFile));
    final Enumeration<JarEntry> entries = in.entries();
    while (entries.hasMoreElements()) {
      final JarEntry ent = entries.nextElement();
      final String name = ent.getName();
      final ZipEntry outEnt = name.endsWith(".jar") ? new ZipEntry(ent) : new ZipEntry(name);
      InputStream entryIn;
      if (name.endsWith(".class") && !shouldSkip(name)) {
        final String className = name.substring(0, name.indexOf(".class")).replace('/', '.');
        //        System.out.println("[className] " + className);
        try {
          final Class c = Class.forName(className, false, ClassLoader.getSystemClassLoader());
          final byte[] modified = InstrumentedClasses.get(className.replace('.', '/'));
          //          System.out.println("[modified] " + modified);
          if (modified == null) {
            entryIn = in.getInputStream(ent);
          } else {
            logger.debug("INSTRUMENTED " + className);
            entryIn = new ByteArrayInputStream(modified);
            outEnt.setSize(modified.length);
            outEnt.setCompressedSize(-1); // unknown
          }
        } catch (final Throwable t) { // NoClassDefFoundError among others
          entryIn = in.getInputStream(ent);
          if (!isTransitive) {
            logger.error("Problem with " + name + ": " + t);
          }
        }
      } else {
        entryIn = in.getInputStream(ent);
      }
      try {
        zout.putNextEntry(outEnt);
      } catch (ZipException e) {
        if (e.getMessage().contains("duplicate")) continue;
        else {
          System.err.println(
              "Error while copying OpenTelemetry file " + outEnt.getName() + "to main JAR.");
          return;
        }
      }
      copy(entryIn, zout);
      entryIn.close();
      zout.closeEntry();
    }
    zout.close();
    in.close();
  }

  private static void copy(final InputStream in, final OutputStream out) throws IOException {
    final byte[] buf = new byte[4 * 1024];
    int read = in.read(buf);
    while (read != -1) {
      out.write(buf, 0, read);
      read = in.read(buf);
    }
  }
}
