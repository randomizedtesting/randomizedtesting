package com.carrotsearch.randomizedtesting.timeouts;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.Utils;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;

public class Test007UncaughtExceptions extends WithNestedTestClass {
  static ThreadGroup parentGroup;

  public static class Nested extends RandomizedTest {
    @Test
    public void checkGlobal() throws Exception {
      assumeRunningNested();

      final Thread t = new Thread(parentGroup, "XYZ") {
        public void run() {
          throw new RuntimeException("Yoda died.");
        }
      };

      t.start();
      t.join();
    }
  }

  @Test
  public void testUncaughtExceptionsAtMainGroup() throws Exception {
    ThreadGroup parentTg = Thread.currentThread().getThreadGroup();
    while (parentTg.getParent() != null)
      parentTg = parentTg.getParent();

    parentGroup = parentTg;
    check();
  }

  @Test
  public void testUncaughtExceptionsAtThreadGroup() throws Exception {
    parentGroup = null;
    check();
  }
  
  /**
   * Apply assertions. 
   */
  private void check() throws Exception {
    FullResult r = runTests(Nested.class);

    Utils.assertFailureWithMessage(r, "Captured an uncaught exception in thread: ");
    Utils.assertFailureWithMessage(r, "Yoda died.");
    Utils.assertFailuresContainSeeds(r);
    Utils.assertNoLiveThreadsContaining("XYZ");

    Assertions.assertThat(getLoggingMessages())
      .contains("Uncaught exception")
      .contains("Yoda died.");
  }
}
