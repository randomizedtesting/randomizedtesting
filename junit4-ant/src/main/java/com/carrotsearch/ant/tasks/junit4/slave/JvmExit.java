package com.carrotsearch.ant.tasks.junit4.slave;

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

    long deadline = System.currentTimeMillis() + 5 * 1000;
    exiter.start();
    
    try {
      while (System.currentTimeMillis() < deadline) {
        Thread.sleep(500);
      }
    } catch (Throwable t) {}
    Runtime.getRuntime().halt(code);
  }
}
