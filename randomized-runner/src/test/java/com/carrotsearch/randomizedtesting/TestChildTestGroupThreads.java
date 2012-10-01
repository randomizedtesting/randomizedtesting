package com.carrotsearch.randomizedtesting;

import java.util.Random;

import org.junit.Test;

/**
 * Make sure we can access contexts from a sub-thread group.
 */
public class TestChildTestGroupThreads extends RandomizedTest {
  volatile int guard;
  
  @Test
  public void testSubgroup() throws Exception {
    ThreadGroup tgroup = new ThreadGroup("child group");
    final Thread t = new Thread(tgroup, "child thread") {
      public void run() {
        Random rnd = RandomizedContext.current().getRandom();
        guard += rnd.nextInt();
      }
    };
    
    t.start();
    t.join();
  }
}
