package com.carrotsearch.ant.tasks.junit4.tests;

import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.Test;

public class TestJvmCrash {
  public static volatile int [] array = new int [0];

  /**
   * Check jvm crash. Simulated via memory seeding with unsafe.
   */
  @SuppressWarnings("all")
  @Test
  public void testEnvVar() throws IOException {
    try {
      sun.misc.Unsafe unsafe = null;
      Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      unsafe = (sun.misc.Unsafe) field.get(null);
      // This causes a crash on J9/ HotSpot, OpenJDK.
      try {
          unsafe.putAddress(0, 1);
      } catch (NullPointerException e) {
        // jrockit probably.
      }

      // For JRockit we have something extra.
      long memPtr = unsafe.allocateMemory(1024 * 8);
      unsafe.freeMemory(memPtr);
      for (int i = 0; i < 1024 * 8; i++) {
        unsafe.putInt(memPtr / 2, 0);
      }
    } catch (Exception e) {
      throw new RuntimeException("Couldn't crash the JVM using Unsafe.", e);
    }
  }
}
