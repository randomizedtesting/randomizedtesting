package com.carrotsearch.ant.tasks.junit4.forked;

import java.io.Serializable;

import com.carrotsearch.randomizedtesting.annotations.SuppressForbidden;

@SuppressForbidden("legitimate sysstreams.")
public class ForkedMainSafe {
  public static void main(String[] args) {
    verifyJUnit4Present();

    try {
      ForkedMain.main(args);
    } catch (Throwable e) {
      try  {
        System.err.println(e.toString());
        e.printStackTrace(System.err);
        System.out.close();
        System.err.close();
      } finally {
        JvmExit.halt(ForkedMain.ERR_EXCEPTION);
      }
    }
  }

  /**
   * Verify JUnit presence and version.
   */
  private static void verifyJUnit4Present() {
    try {
      Class<?> clazz = Class.forName("org.junit.runner.Description");
      if (!Serializable.class.isAssignableFrom(clazz)) {
        JvmExit.halt(ForkedMain.ERR_OLD_JUNIT);
      }
    } catch (ClassNotFoundException e) {
      JvmExit.halt(ForkedMain.ERR_NO_JUNIT);
    }
  }  
}
