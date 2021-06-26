package io.opentelemetry.javaagent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class PreTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(final ClassLoader loader, final String className,
        final Class<?> classBeingRedefined,
        final ProtectionDomain protectionDomain, final byte[] classfileBuffer)
        throws IllegalClassFormatException {
        System.out.println("[PreTransformer] " + className);
        StaticInstrumenter.CurrentClass.set(new BytesAndName(classfileBuffer, className));
        return null;
    }
}
