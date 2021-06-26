package io.opentelemetry.javaagent;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class StaticInstrumenter {
  // FIXME classloaders in use by loaded jars, classloader as key
  // key is slashy name, not dotty
  public static final Map<String, byte[]> InstrumentedClasses = new ConcurrentHashMap<>();

  public static final ThreadLocal<BytesAndName> CurrentClass = new ThreadLocal<>();

  public static ClassFileTransformer getPreTransformer() {
    return new PreTransformer();
  }

  public static ClassFileTransformer getPostTransformer() {
    return new PostTransformer();
  }

  // Above happens in -javaagent-space, below in main-space
  public static void main(final String[] args) throws Exception {

    System.out.println("[CLASSPATH] " + System.getProperty("java.class.path"));


    // FIXME error handling, user niceties, etc.
    final File outDir = new File(args[0]);
    if (!outDir.exists()) {
      outDir.mkdir();
    }

    for (final String pathItem : System.getProperty("java.class.path")
        .split(System.getProperty("path.separator"))) {
      System.out.println("[PATH_ITEM] " + pathItem);
      // FIXME java 9 / jmod support, proper handling of directories, just generally better and more resilient stuff
      // FIXME jmod in particular introduces weirdness with adding helpers to the dependencies
      if (pathItem.endsWith(".jar")) {
        processJar(new File(pathItem), outDir);
      }
    }
  }

  private static boolean shouldSkip(final String entryName) {
    return entryName.startsWith("io/opentelemetry");
  }

  private static void processJar(final File jar, final File outDir) throws Exception {
    System.out.println("[processJar] " + jar.getName());
    // FIXME don't "instrument" our agent jar.
    final File outFile = new File(outDir, jar.getName()); // FIXME multiple jars with same name
    // FIXME detect and warn on signed jars (and drop the signing bits)

    final JarFile in = new JarFile(jar);
    final ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(outFile));
    final Enumeration<JarEntry> entries = in.entries();
    while (entries.hasMoreElements()) {
      final JarEntry ent = entries.nextElement();
      final String name = ent.getName();
      final ZipEntry outEnt = new ZipEntry(ent);
      InputStream entryIn = null;
      if (name.endsWith(".class") && !shouldSkip(name)) {
        final String className = name.substring(0, name.indexOf(".class")).replace('/', '.');
        System.out.println("[className] " + className);
        try {
          final Class c = Class.forName(className, false, ClassLoader.getSystemClassLoader());
          final byte[] modified = InstrumentedClasses.get(className.replace('.', '/'));
          System.out.println("[modified] " + modified);
          if (modified == null) {
            entryIn = in.getInputStream(ent);
          } else {
            System.out.println("INSTRUMENTED " + className);
            entryIn = new ByteArrayInputStream(modified);
            outEnt.setSize(modified.length);
            outEnt.setCompressedSize(-1); // unknown
          }
        } catch (final Throwable t) { // NoClassDefFoundError among others
          entryIn = in.getInputStream(ent);
          System.out.println("Problem with " + name + ": " + t);
        }
      } else {
        entryIn = in.getInputStream(ent);
      }
      zout.putNextEntry(outEnt);
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
