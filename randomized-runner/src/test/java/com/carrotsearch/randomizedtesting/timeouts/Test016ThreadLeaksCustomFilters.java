package com.carrotsearch.randomizedtesting.timeouts;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.ThreadFilter;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakFilters;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;

/**
 * Checks custom thread ignore policy.
 */
public class Test016ThreadLeaksCustomFilters extends WithNestedTestClass {
  public static class FooBarFilter implements ThreadFilter {
    @Override
    public boolean reject(Thread t) {
      return t.getName().contains("foobar");
    }
  }

  @ThreadLeakScope(Scope.TEST)
  @ThreadLeakFilters(defaultFilters = true, filters = {
      FooBarFilter.class
  })
  public static class Nested1 extends RandomizedTest {
    @Test
    public void testFooBars() throws Exception {
      assumeRunningNested();
      for (int i = 0; i < 10; i++) {
        startZombieThread("foobar-" + i);
      }
    }
  }

  @Test
  public void testFilteredOnly() throws Throwable {
    Assertions.assertThat(runTests(Nested1.class).getFailures()).isEmpty();

    Assertions.assertThat(getLoggingMessages()).isEmpty();
    Assertions.assertThat(getSysouts()).isEmpty();
  }
}
