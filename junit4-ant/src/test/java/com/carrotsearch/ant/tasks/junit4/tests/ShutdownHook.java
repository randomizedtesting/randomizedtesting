package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Test;

public class ShutdownHook {
  @Test
  public void testShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        // Delay by about a minute.
        long deadline = System.currentTimeMillis() + 60 * 1000;
        while (System.currentTimeMillis() < deadline) {
          try {
            Thread.sleep(1000);
          } catch (Exception e) {}
        }
      }
    });
  }
}
