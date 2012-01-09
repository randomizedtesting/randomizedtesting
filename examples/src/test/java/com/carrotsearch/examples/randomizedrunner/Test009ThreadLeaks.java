package com.carrotsearch.examples.randomizedrunner;

import java.util.concurrent.*;

import org.junit.*;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.RandomizedTest;

/**
 * Leaked background threads can crash tests randomly even if all seeds are
 * known and predictable. They can also affect other test cases and so we should
 * not allow any threads to "leak outside" of their scope (a single test case or
 * a single suite if the thread is started in {@link BeforeClass} or
 * {@link AfterClass} hooks).
 * 
 * <p>
 * {@link RandomizedRunner} has built-in support for detecting threads that
 * escaped their current {@link ThreadGroup}'s boundary. Such threads are killed
 * and make the test (or suite) fail with appropriate exception.
 * {@link #leftOverThread()} method below shows a simple scenario in which a
 * test leaks outside its test boundary. A correct code that calls {@link Thread#join()}
 * is shown in {@link #noLeakHere()}.
 *
 * <p>
 * More concepts concerning leaking threads and some workarounds for typical
 * problems with them is shown in {@link Test010Lingering}. 
 */
public class Test009ThreadLeaks extends RandomizedTest {
  @Test
  public void leftOverThread() throws Exception {
    final CountDownLatch go = new CountDownLatch(1);
    
    Thread t = new Thread() {
      public void run() {
        try {
          go.countDown();
          Thread.sleep(1000);
        } catch (InterruptedException e) {/* ignore */}
      }
    };

    // Start and wait for it to really start.
    t.start();
    go.await();
  }

  @Test
  public void noLeakHere() throws Exception {
    final CountDownLatch go = new CountDownLatch(1);
    
    Thread t = new Thread() {
      public void run() {
        try {
          go.countDown();
          Thread.sleep(1000);
        } catch (InterruptedException e) {/* ignore */}
      }
    };

    // Start and wait for it to really start.
    t.start();
    go.await();
    t.join();
  }
}
