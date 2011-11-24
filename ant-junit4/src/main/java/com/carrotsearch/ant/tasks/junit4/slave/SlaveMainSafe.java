package com.carrotsearch.ant.tasks.junit4.slave;

public class SlaveMainSafe {
  public static void main(String[] args) {
    try {
      SlaveMain.main(args);
    } catch (Throwable e) {
      System.out.close();
      if (e instanceof NoClassDefFoundError && e.getMessage().contains("org/junit/")) {
        System.exit(SlaveMain.ERR_NO_JUNIT);
      }
      System.err.println(e.toString());
      e.printStackTrace(System.err);
      System.exit(SlaveMain.ERR_EXCEPTION);
    }
  }
}
