package com.carrotsearch.randomizedtesting.timeouts;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;
import com.carrotsearch.randomizedtesting.annotations.TimeoutSuite;

/**
 * Checks custom thread ignore policy.
 */
public class Test018TimeoutStacks extends WithNestedTestClass {
  @ThreadLeakScope(Scope.TEST)
  @TimeoutSuite(millis = 1000)
  public static class Nested1 {
    @Test
    public void testFooBars() throws Exception {
      assumeRunningNested();
      for (int i = 0; i < 5; i++) {
        startThread("foobar-" + i);
      }
      Thread.sleep(5000);
    }
  }

  @Test
  public void testExceptionInFilter() throws Throwable {
    new JUnitCore().run(new RandomizedRunner(Nested1.class));
    Assertions.assertThat(getLoggingMessages()).contains("sleepForever(");
    // sysout.println(getLoggingMessages());
  }    
}
