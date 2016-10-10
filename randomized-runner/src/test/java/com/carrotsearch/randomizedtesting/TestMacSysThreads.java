package com.carrotsearch.randomizedtesting;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.annotations.ThreadLeakAction;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakAction.Action;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;

/**
 * Test Mac system threads.
 */
public class TestMacSysThreads extends WithNestedTestClass {
  @ThreadLeakScope(Scope.SUITE)
  @ThreadLeakAction({Action.WARN})
  public static class Nested extends RandomizedTest {
    @Test
    public void testMethod1() {
      MBeanServer mb = ManagementFactory.getPlatformMBeanServer();
      mb.getMBeanCount();
      RandomizedTest.sleep(2500);
    }
  }

  @Test
  public void testSuccessful() {
    checkTestsOutput(1, 0, 0, 0, Nested.class);
  }
}
