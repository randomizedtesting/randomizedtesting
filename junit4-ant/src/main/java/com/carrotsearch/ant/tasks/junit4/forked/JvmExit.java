package com.carrotsearch.ant.tasks.junit4.forked;

import java.util.concurrent.TimeUnit;

final class JvmExit {
  final static void halt(final int code) {
    // try to exit gracefully by calling system.exit. If we terminate within 5 seconds, fine.
    // If not, halt the JVM.
    final Thread exiter = new Thread() {
      @Override
      public void run() {
        System.exit(code);
      }
    };

    final long duration = TimeUnit.SECONDS.toNanos(5);
    final long startTime = System.nanoTime();
    exiter.start();
    
    try {
      while (System.nanoTime() - startTime < duration) {
        Thread.sleep(500);
      }
    } catch (Throwable t) {}
    Runtime.getRuntime().halt(code);
  }
}
