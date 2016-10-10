package com.carrotsearch.randomizedtesting;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.annotations.ThreadLeakAction;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakAction.Action;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;

/**
 * Test {@link Test#expected()}.
 */
public class TestJ9SysThreads extends WithNestedTestClass {
  @ThreadLeakScope(Scope.SUITE)
  @ThreadLeakAction({Action.INTERRUPT})
  public static class Nested extends RandomizedTest {
    @Test
    public void testMethod1() {
      assumeRunningNested();

      for (MemoryPoolMXBean mbean : ManagementFactory.getMemoryPoolMXBeans()) {
        mbean.getUsage();
        sysout.println(mbean.getClass().getName());
      }
    }
  }

  @Test
  public void testSuccessfulExceptedFailure() {
    checkTestsOutput(1, 0, 0, 0, Nested.class);
  }
}
