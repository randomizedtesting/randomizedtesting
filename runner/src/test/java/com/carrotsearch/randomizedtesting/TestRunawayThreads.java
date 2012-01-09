package com.carrotsearch.randomizedtesting;

import java.util.concurrent.*;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.*;
import org.junit.runner.notification.Failure;

import com.carrotsearch.randomizedtesting.annotations.ThreadLeaks;

@RunWith(RandomizedRunner.class)
public class TestRunawayThreads extends WithNestedTestClass {
  private abstract static class ThreadWithException extends Thread {
    public volatile Throwable throwable;
    
    @Override
    public final void run() {
      try {
        runWrapped();
      } catch (Throwable t) {
        throwable = t;
      }
    }
    
    public final ThreadWithException startAndJoin() throws Throwable {
      this.start();
      try {
        this.join(5000);
        if (throwable != null) throw throwable;
      } catch (InterruptedException e) {
        throw e;
      }
      return this;
    }
    
    protected abstract void runWrapped();
  }

  @Test
  public void subThreadContextPropagation() throws Throwable {
    final long seed = RandomizedContext.current().getRunnerRandomness().seed;
    new ThreadWithException() {
      protected void runWrapped() {
        RandomizedContext ctx = RandomizedContext.current();
        Assert.assertEquals(seed, ctx.getRandomness().getSeed());
      }
    }.startAndJoin();
  }

  @ThreadLeaks(linger = 2000)
  @Test
  public void ExecutorServiceContextPropagation() throws Throwable {
    final long seed = RandomizedContext.current().getRunnerRandomness().seed;
    final ExecutorService executor = Executors.newCachedThreadPool();
    try {
      executor.submit(new Runnable() {
        public void run() {
          RandomizedContext ctx = RandomizedContext.current();
          Assert.assertEquals(seed, ctx.getRandomness().getSeed());
        }
      }).get();
    } catch (ExecutionException e) {
      throw e.getCause();
    } finally {
      executor.shutdown();
      executor.awaitTermination(1, TimeUnit.SECONDS);
    }
  }
  
  public static class Nested extends RandomizedTest {
    final boolean withJoin;

    public Nested() {
      this(true);
    }

    protected Nested(boolean withJoin) {
      this.withJoin = withJoin;
    }

    @Test @ThreadLeaks(stackSamples = 0)
    public void spinoffAndThrow() throws Exception{
      assumeRunningNested();
      Thread t = new Thread() {
        public void run() {
          RandomizedTest.sleep(500);
          throw new RuntimeException("spinoff exception");
        }
      };
      t.start();
      if (withJoin) {
        t.join();
      }
    }
  }
  
  public static class NestedNoJoin extends Nested {
    public NestedNoJoin() {
      super(false);
    }
  }

  @Test
  public void subUncaughtExceptionInSpunOffThread() throws Throwable {
    Result r = JUnitCore.runClasses(Nested.class);
    Assert.assertEquals(1, r.getFailureCount());
    Failure testFailure = r.getFailures().get(0);
    Throwable testException = testFailure.getException();
    Throwable threadException = testException.getCause();
    Assert.assertNotNull(RandomizedRunner.seedFromThrowable(testException));
    Assert.assertNotNull(RandomizedRunner.seedFromThrowable(threadException));
  }

  @Test
  public void subNotJoined() throws Throwable {
    Result r = JUnitCore.runClasses(NestedNoJoin.class);
    Assert.assertEquals(1, r.getFailureCount());
    Failure testFailure = r.getFailures().get(0);
    Throwable testException = testFailure.getException();
    Assert.assertNotNull(RandomizedRunner.seedFromThrowable(testException));
  }  
  
  public static class NestedClassScope extends RandomizedTest {
    @BeforeClass
    public static void startThread() {
      if (!isRunningNested())
        return;

      new Thread() {
        @Override
        public void run() {
          RandomizedTest.sleep(5000);
        }
      }.start();
    }

    @Test
    public void spinoffAndThrow() throws Exception{
      assumeRunningNested();
    }
  }
  
  @Test
  public void subNotJoinOnClassLevel() throws Throwable {
    Result r = JUnitCore.runClasses(NestedClassScope.class);
    Assert.assertEquals(1, r.getFailureCount());
    Failure testFailure = r.getFailures().get(0);
    Throwable testException = testFailure.getException();
    Assert.assertNotNull(RandomizedRunner.seedFromThrowable(testException));
  }    
}
