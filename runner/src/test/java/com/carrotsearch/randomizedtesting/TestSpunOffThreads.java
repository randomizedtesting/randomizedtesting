package com.carrotsearch.randomizedtesting;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RandomizedRunner.class)
public class TestSpunOffThreads extends WithNestedTestClass {
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
        if (throwable != null)
          throw throwable;
      } catch (InterruptedException e) {
        throw e;
      }
      return this;
    }

    protected abstract void runWrapped();
  }
  
  @Test
  public void subThreadContextPropagation() throws Throwable {
    final long seed = RandomizedContext.current().getRandomness().getSeed();
    new ThreadWithException() {
      protected void runWrapped() {
        RandomizedContext ctx = RandomizedContext.current();
        Assert.assertEquals(seed, ctx.getRandomness().getSeed());
      }
    }.startAndJoin();
  }
}
