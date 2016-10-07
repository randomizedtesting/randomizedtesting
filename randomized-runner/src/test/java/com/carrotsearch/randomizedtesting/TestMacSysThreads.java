package com.carrotsearch.randomizedtesting;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.Result;

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
      RandomizedTest.sleep(5000);
    }
  }

  @Test
  public void testSuccessful() {
    Result result = runClasses(Nested.class);
    Assert.assertEquals(0, result.getFailureCount());
  }
}
