package com.carrotsearch.examples.randomizedrunner;

import java.util.concurrent.*;

import org.junit.Assert;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakAction;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakLingering;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakZombies;

/**
 * In many cases the creation of background threads lies beyond our control (and
 * their direct termination or even awaiting for their termination) is not
 * possible. A good example is the {@link Executors} framework where background
 * {@link ThreadFactory} is not published if not given explicitly. 
 * To handle such situations {@link RandomizedRunner} needs to know if it should
 * await background thread to complete (and for how long) or if it should allow
 * them to "leak" safely and not complain. All this can be declared with a set
 * of annotations starting with <code>ThreadLeak*</code> prefix.
 * 
 * <p>We can use {@link ThreadLeakLingering} annotation instead of explicitly using 
 * {@link Thread#join()} sometimes. For example, {@link Test009ThreadLeaks#leftOverThread()}
 * can be rewritten as shown in {@link #lingerForLeftOverThread()}.
 * 
 * <p>The same annotation can be used to wait for background threads 
 * which we don't have any control on, but we know they will eventually terminate.
 * For example, a terminated {@link Executor} does not wait (join) with its slave
 * threads so lingering here is required. This is shown in method {@link #executorLeak()}. This
 * method will fail (from time to time, it isn't guaranteed) if no lingering time is given.
 * 
 * <p>There are other annotations for advanced control of thread leaks and their outcomes, check out
 * the javadocs in the links below. 
 * 
 * @see ThreadLeakScope
 * @see ThreadLeakLingering
 * @see ThreadLeakAction
 * @see ThreadLeakZombies
 */
public class Test010Lingering extends RandomizedTest {
  
  @Test @ThreadLeakLingering(linger = 2000)
  public void lingerForLeftOverThread() throws Exception {
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

  // @ThreadLeaks(linger = 1000) // Enable me to make it pass all the time.
  @Test @Repeat(iterations = 10)
  public void executorLeak() throws Exception {
    int numThreads = 50;
    final ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    for (int i = 0; i < 2 * numThreads; i++) {
      executor.submit(new Runnable() {
        public void run() {
          sleep(10);
        }
      });
    }

    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.SECONDS);
    Assert.assertTrue(executor.isShutdown());
    Assert.assertTrue(executor.isTerminated());
  }
}
