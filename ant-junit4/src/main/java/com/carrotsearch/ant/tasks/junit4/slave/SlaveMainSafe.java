package com.carrotsearch.ant.tasks.junit4.slave;

import java.io.Serializable;

public class SlaveMainSafe {
  public static void main(String[] args) {
    verifyJUnit4Present();

    try {
      SlaveMain.main(args);
    } catch (Throwable e) {
      System.out.close();
      System.err.println(e.toString());
      e.printStackTrace(System.err);
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
