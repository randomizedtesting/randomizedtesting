package com.carrotsearch.randomizedtesting.timeouts;

import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.*;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;

@RunWith(RandomizedRunner.class)
public class Test012RunawayThreadsKilledAtOnce extends WithNestedTestClass {
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

            try {
              Thread.sleep(20000);
            } catch (InterruptedException e) {
              // Ignore
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
    FullResult result = runTests(NestedClass.class);
    long end = System.currentTimeMillis();

    Assert.assertEquals(1, result.getFailureCount());
    Assert.assertTrue((end - start) + " msec?", (end - start) < 1000 * 10);
  }
}
