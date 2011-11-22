package com.carrotsearch.randomizedtesting.examples;

import java.util.concurrent.*;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeaks;

public class TestThreadLeaks extends RandomizedTest {
  @Test @Repeat(iterations = 10)
  @ThreadLeaks(linger = 2000)
  public void leakInExecutors() throws Exception {
    ExecutorService exec = Executors.newCachedThreadPool();
    for (int i = 0; i < 5; i++) {
      exec.submit(new Runnable() {
        public void run() {
          sleep(100);
        }
      });
    }

    // "Orderly" shutdown. Wait for executing tasks.
    exec.shutdown();
    exec.awaitTermination(5, TimeUnit.SECONDS);
    assertTrue(exec.isShutdown());
    assertTrue(exec.isTerminated());
  }
}
