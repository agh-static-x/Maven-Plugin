/*
 * SPDX-License-Identifier: Apache-2.0
 */
package io.opentelemetry.javaagent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;

public class PostTransformer implements ClassFileTransformer {
  @Override
  public byte[] transform(
      final ClassLoader loader,
      final String className,
      final Class<?> classBeingRedefined,
      final ProtectionDomain protectionDomain,
      final byte[] classfileBuffer)
      throws IllegalClassFormatException {
    final BytesAndName pre = StaticInstrumenter.CurrentClass.get();
    //    System.out.println("[PostTransformer] " + className);
    //    if (pre != null) {
    //      System.out.println("classname equal: " + (pre.name.equals(className) ? "YES" : "NO"));
    //      System.out.println(
    //          "bytecode modified: " + (!Arrays.equals(pre.bytes, classfileBuffer) ? "YES" :
    // "NO"));
    //    }

    if (pre != null && pre.name.equals(className) && !Arrays.equals(pre.bytes, classfileBuffer)) {
      StaticInstrumenter.InstrumentedClasses.put(className, classfileBuffer);
    }
    return null;
  }
}
