package com.carrotsearch.ant.tasks.junit4.slave;

import java.io.Serializable;

import com.carrotsearch.randomizedtesting.annotations.SuppressForbidden;

@SuppressForbidden("legitimate sysstreams.")
public class SlaveMainSafe {
  public static void main(String[] args) {
    verifyJUnit4Present();

    try {
      SlaveMain.main(args);
    } catch (Throwable e) {
      try  {
        System.err.println(e.toString());
        e.printStackTrace(System.err);
        System.out.close();
        System.err.close();
      } finally {
        JvmExit.halt(SlaveMain.ERR_EXCEPTION);
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
        JvmExit.halt(SlaveMain.ERR_OLD_JUNIT);
      }
    } catch (ClassNotFoundException e) {
      JvmExit.halt(SlaveMain.ERR_NO_JUNIT);
    }
  }  
}
