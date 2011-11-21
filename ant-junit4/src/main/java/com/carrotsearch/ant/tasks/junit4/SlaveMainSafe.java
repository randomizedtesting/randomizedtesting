package com.carrotsearch.ant.tasks.junit4;

public class SlaveMainSafe {
  public static void main(String[] args) {
    try {
      SlaveMain.main(args);
    } catch (NoClassDefFoundError e) {
      System.err.println("Could not launch SlaveMain: " + e.toString());
      if (e.getMessage().contains("org/junit/")) {
        System.exit(SlaveMain.ERR_NO_JUNIT);
      } else {
        System.exit(SlaveMain.ERR_EXCEPTION);
      }
    }
  }
}
