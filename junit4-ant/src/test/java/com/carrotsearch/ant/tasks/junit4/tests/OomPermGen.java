package com.carrotsearch.ant.tasks.junit4.tests;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_6;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assume;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

public class OomPermGen {
  abstract class WhoaClassLoader extends ClassLoader {
    public abstract Class<?> defineNewClass(byte [] clazz) throws ClassNotFoundException;
  }

  WhoaClassLoader cl = new WhoaClassLoader() {
    public Class<?> defineNewClass(byte [] clazz) throws ClassNotFoundException {
      return super.defineClass(null, clazz, 0, clazz.length);
    }
  };

  @Test
  public void explodePermGen() throws Exception {
    MemoryPoolMXBean permGenPool = null;
    for (MemoryPoolMXBean mp : ManagementFactory.getMemoryPoolMXBeans()) {
      if (mp.getName().contains("Perm Gen")) {
        permGenPool = mp;
        break;
      }
    }

    if (permGenPool == null) {
      // No permgen pool?
      Assume.assumeTrue(false);
    }

    // Just keep on loading classes until we explode.
    Random rnd = new Random(0xdeadbeef);
    int classes = 0;
    List<Class<?>> keep = new ArrayList<Class<?>>();
    while (true) {
      if ((classes++ % 1000) == 0) {
        System.out.println(permGenPool.getName() + " => " + permGenPool.getUsage());
      }

      // Prepare a new unique exception class with asm. Throw it.
      Class<?> clz = generateExceptionClass(rnd.nextLong());
      clz.newInstance();
      keep.add(clz);
    }
  }

  public Class<?> generateExceptionClass(long x) throws Exception {
    ClassWriter cw = new ClassWriter(0);
    MethodVisitor mv;
    cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER,
        "com/carrotsearch/ant/tasks/junit4/tests/SyntheticException_" + x, null,
        "java/lang/Exception", null);

    mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Exception", "<init>", "()V", false);
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
    cw.visitEnd();

    return cl.defineNewClass(cw.toByteArray());
  }
}
