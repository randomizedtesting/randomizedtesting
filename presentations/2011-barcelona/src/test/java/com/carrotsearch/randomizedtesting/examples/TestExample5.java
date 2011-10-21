package com.carrotsearch.randomizedtesting.examples;

import org.junit.Ignore;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Timeout;

@Ignore // Remove to enable the example. Disabled to speed up tests.
public class TestExample5 extends RandomizedTest {
  @Test @Timeout(millis = 1000)
  public void interruptable() throws Exception {
    System.err.println("--");
    Thread.sleep(100000);
  }

  @Test @Timeout(millis = 1000)
  public void uninterruptable() throws Exception {
    System.err.println("--");
    while (true) /* spin */;
  }

  @Test @Timeout(millis = 1000)
  public void christopherLambert() throws Exception {
    System.err.println("--");
    while (true) {
      try { Thread.sleep(1000); } catch (Throwable t) {}
    }
  }

  @Test
  public void leftOverSubThread() throws Exception {
    System.err.println("--");
    new Thread() {
      public void run() {
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
      }
    }.start();

    // Make sure it's really started.
    Thread.sleep(500);
  }
}

