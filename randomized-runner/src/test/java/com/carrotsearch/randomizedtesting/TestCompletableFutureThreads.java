package com.carrotsearch.randomizedtesting;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.annotations.ThreadLeakAction;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakAction.Action;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;

/**
 * Test that {@link CompletableFuture}'s delayed executor thread is not reported as a leak
 * ("CompletableFutureDelayScheduler" on JDK &lt; 25, "ForkJoinPool.commonPool-delayScheduler"
 * on JDK 25+).
 */
public class TestCompletableFutureThreads extends WithNestedTestClass {
  @ThreadLeakScope(Scope.SUITE)
  @ThreadLeakAction({Action.INTERRUPT})
  public static class Nested extends RandomizedTest {
    @Test
    public void testMethod1() throws Exception {
      assumeRunningNested();

      // completeOnTimeout is Java 9+; invoked reflectively so this compiles and runs on Java 8,
      // where the delay scheduler thread does not exist.
      Method completeOnTimeout;
      try {
        completeOnTimeout = CompletableFuture.class.getMethod(
            "completeOnTimeout", Object.class, long.class, TimeUnit.class);
      } catch (NoSuchMethodException e) {
        return;
      }

      CompletableFuture<Object> cf = new CompletableFuture<Object>();
      ((CompletableFuture<?>) completeOnTimeout.invoke(
          cf, null, 1L, TimeUnit.MILLISECONDS)).join();
    }
  }

  @Test
  public void testDelaySchedulerNotReportedAsLeak() {
    checkTestsOutput(1, 0, 0, 0, Nested.class);
  }
}
