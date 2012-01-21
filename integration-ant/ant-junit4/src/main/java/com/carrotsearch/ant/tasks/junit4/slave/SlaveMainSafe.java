package com.carrotsearch.ant.tasks.junit4.slave;

import java.io.PrintStream;
import java.io.Serializable;

public class SlaveMainSafe {
  public static void main(String[] args) {
    verifyJUnit4Present();

    PrintStream serr = System.err;
    try {
      SlaveMain.main(args);
    } catch (Throwable e) {
      serr.println(e.toString());
      e.printStackTrace(serr);

      System.out.close();
      System.err.close();
      System.exit(SlaveMain.ERR_EXCEPTION);
    }
  }

  /**
   * Verify JUnit presence and version.
   */
  private static void verifyJUnit4Present() {
    try {
      Class<?> clazz = Class.forName("org.junit.runner.Description");
      if (!Serializable.class.isAssignableFrom(clazz)) {
        System.exit(SlaveMain.ERR_OLD_JUNIT);
      }
    } catch (ClassNotFoundException e) {
      System.exit(SlaveMain.ERR_NO_JUNIT);
    }
  }  
}
