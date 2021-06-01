package io.opentelemetry.javaagent.bootstrap;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class StaticInstrumenter {

    public static class BytesAndName {
        public byte[] bytes;
        public String name;

        public BytesAndName(final byte[] bytes, final String name) {
            this.bytes = bytes;
            this.name = name;
        }

    }

    // FIXME classloaders in use by loaded jars, classloader as key
    // key is slashy name, not dotty
    public static final Map<String, byte[]> InstrumentedClasses = new ConcurrentHashMap<>();

    public static final ThreadLocal<BytesAndName> CurrentClass = new ThreadLocal<>();

    public static class PreTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(final ClassLoader loader, final String className,
                                final Class<?> classBeingRedefined,
                                final ProtectionDomain protectionDomain, final byte[] classfileBuffer)
                throws IllegalClassFormatException {
            System.out.println(className);
            CurrentClass.set(new BytesAndName(classfileBuffer, className));
            return null;
        }
    }

    public static class PostTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(final ClassLoader loader, final String className,
                                final Class<?> classBeingRedefined,
                                final ProtectionDomain protectionDomain, final byte[] classfileBuffer)
                throws IllegalClassFormatException {
            final BytesAndName pre = CurrentClass.get();
            if (pre != null && pre.name.equals(className) && !Arrays.equals(pre.bytes, classfileBuffer)) {
                InstrumentedClasses.put(className, classfileBuffer);
            }
            return null;
        }

    }

    public static ClassFileTransformer getPreTransformer() {
        return new PreTransformer();
    }

    public static ClassFileTransformer getPostTransformer() {
        return new PostTransformer();
    }

    // Above happens in -javaagent-space, below in main-space
    public static void main(final String[] args) throws Exception {

        // FIXME error handling, user niceties, etc.
        final File outDir = new File(args[0]);
        if (!outDir.exists()) {
            outDir.mkdir();
        }

        for (final String pathItem : System.getProperty("java.class.path")
                .split(System.getProperty("path.separator"))) {
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
                try {
                    final Class c = Class.forName(className, false, ClassLoader.getSystemClassLoader());
                    final byte[] modified = InstrumentedClasses.get(className.replace('.', '/'));
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
