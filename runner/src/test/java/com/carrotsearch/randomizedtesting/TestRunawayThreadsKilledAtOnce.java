package com.carrotsearch.randomizedtesting;

import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.*;

@RunWith(RandomizedRunner.class)
public class TestRunawayThreadsKilledAtOnce extends WithNestedTestClass {
  @RunWith(RandomizedRunner.class)
  public static class NestedClass {
    @Test
    public void lotsOfStubbornThreads() throws Throwable {
      assumeRunningNested();
      final CountDownLatch latch = new CountDownLatch(50);

      Thread [] threads = new Thread [(int) latch.getCount()];
      for (int i = 0; i < threads.length; i++) {
        threads[i] = new Thread("stubborn-" + i) {
          @Override
          public void run() {
            latch.countDown();

            while (true) {
              try {
                Thread.sleep(1);
              } catch (InterruptedException e) {
                // Ignore
              }
            }
          }
        };
        threads[i].start();
      }

      // Wait for all threads to be really started.
      latch.await();
    }
  }

  @Test
  public void testLotsOfStubbornThreads() {
    long start = System.currentTimeMillis();
    Result result = JUnitCore.runClasses(NestedClass.class);
    long end = System.currentTimeMillis();

    Assert.assertEquals(50, result.getFailureCount());
    Assert.assertTrue((end - start) + " msec?", (end - start) < 1000 * 10);
  }
}
