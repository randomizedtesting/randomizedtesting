package com.carrotsearch.ant.tasks.junit4.tests;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_6;

import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

public class TestFailureTypePassing {
  @Test
  public void nonStandardError() throws Exception {
    // Prepare a new unique exception class with asm. Throw it.
    Class<?> clz = generateExceptionClass();
    throw Exception.class.cast(clz.newInstance());
  }

  public static Class<?> generateExceptionClass() throws Exception {
    ClassWriter cw = new ClassWriter(0);
    MethodVisitor mv;
    cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER,
        "com/carrotsearch/ant/tasks/junit4/tests/SyntheticException", null,
        "java/lang/Exception", null);
    
    mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Exception", "<init>", "()V", false);
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
    cw.visitEnd();

    return new ClassLoader() {
      public Class<?> defineNewClass(byte [] clazz) throws ClassNotFoundException {
        return super.defineClass(null, clazz, 0, clazz.length);
      }
    }.defineNewClass(cw.toByteArray());
  }
}
