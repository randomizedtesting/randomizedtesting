package com.carrotsearch.randomizedtesting;

import java.util.Random;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.annotations.Repeat;

/**
 * Make sure we don't OOM even if we recreate many, many threads during a test/ suite.
 */
public class TestManyThreadsOOM extends RandomizedTest {
  volatile int guard;
  
  @Test
  @Repeat(iterations = 10)
  public void testSpawnManyThreads() throws Exception {
    // create threads sequentially, attaching a big blob of data to each, then
    // touch the context from within a thread and die.
    for (int i = 0; i < 500; i++) {
      final Thread t = new Thread() {
        final byte [] hold = new byte [1024 * 1024 * 10];

        public void run() {
          Random rnd = RandomizedContext.current().getRandom();
          guard += rnd.nextInt() + hold.length;
        }
      };

      t.start();
      t.join();
    }
  }
}
