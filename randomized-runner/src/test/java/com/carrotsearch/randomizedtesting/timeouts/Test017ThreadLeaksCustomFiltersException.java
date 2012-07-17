package com.carrotsearch.randomizedtesting.timeouts;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.ThreadFilter;
import com.carrotsearch.randomizedtesting.Utils;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakFilters;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;

/**
 * Checks custom thread ignore policy.
 */
public class Test017ThreadLeaksCustomFiltersException extends WithNestedTestClass {
  public static class ExceptionFilter implements ThreadFilter {
    @Override
    public boolean reject(Thread t) {
      throw new RuntimeException("filter-exception");
    }
  }

  @ThreadLeakScope(Scope.TEST)
  @ThreadLeakFilters(defaultFilters = true, filters = {
      ExceptionFilter.class
  })
  public static class Nested1 {
    @Test
    public void testFooBars() throws Exception {
      assumeRunningNested();
      for (int i = 0; i < 10; i++) {
        startZombieThread("foobar-" + i);
      }
    }
  }

  @Test
  public void testExceptionInFilter() throws Throwable {
    Result r = new JUnitCore().run(new RandomizedRunner(Nested1.class));
    Utils.assertFailureWithMessage(r, "filter-exception");
  }    
}
