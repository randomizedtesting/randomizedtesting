package com.carrotsearch.ant.tasks.junit4.tests;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;


public class SlaveHangingBackgroundThreads {
  static {
    final CountDownLatch latch = new CountDownLatch(1);
    new Thread("background-non-daemon") {
      public void run() {
        System.out.println("Starting.");
        try {
          latch.countDown();
          // Wait looong.
          Thread.sleep(1000 * 60 * 60);
        } catch (Exception e) {
          // empty.
        }
      }
    }.start();

    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    // Cause an unchecked exception.
    @SuppressWarnings("unused")
    int a = 2 / 0;
  }
  
  @Test
  public void method() {
    // Ok, passes.
  }
}
