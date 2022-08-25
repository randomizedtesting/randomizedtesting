package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class ShutdownHook {
  @Test
  public void testShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        // Delay by about a minute.
        final long duration = TimeUnit.SECONDS.toNanos(60);
        final long startTime = System.nanoTime();
        while (System.nanoTime() - startTime < duration) {
          try {
            Thread.sleep(1000);
          } catch (Exception e) {}
        }
      }
    });
  }
}
