package com.carrotsearch.ant.tasks.junit4.slave;

import java.io.PrintStream;
import java.io.Serializable;

public class SlaveMainSafe {
  public static void main(String[] args) {
    verifyJUnit4Present();

    PrintStream serr = System.err;
    try {
      serr.println("# Initializing.");
      try {
        SlaveMain.main(args);
      } finally {
        serr.println("# Done.");
      }
    } catch (Throwable e) {
      try  {
        serr.println(e.toString());
        e.printStackTrace(serr);
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
