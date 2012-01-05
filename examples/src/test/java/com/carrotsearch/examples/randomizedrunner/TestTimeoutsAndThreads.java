package com.carrotsearch.examples.randomizedrunner;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Timeout;

/**
 * Shows how {@link RandomizedRunner} handles runaway subthreads and
 * test timeouts.
 */
public class TestTimeoutsAndThreads extends RandomizedTest {
  @Test @Timeout(millis = 1000)
  public void tooLongTest() throws Exception {
    Thread.sleep(100000);
  }

  @Test
  public void leftOverThread() throws Exception {
    new Thread() {
      public void run() {
        try { 
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          // empty.
        }
      }
    }.start();
    Thread.sleep(200);
  }
}

