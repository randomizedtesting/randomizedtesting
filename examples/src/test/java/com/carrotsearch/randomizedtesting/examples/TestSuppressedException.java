package com.carrotsearch.randomizedtesting.examples;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.RandomizedRunner;

/**
 * Check what happens if an exception occurs in the test case _and_ in after and in afterclass...
 */
@RunWith(RandomizedRunner.class)
public class TestSuppressedException {
  @AfterClass
  public static void afterClass() {
    throw new RuntimeException("afterClass exception");
  }

  @After
  public void after() {
    throw new RuntimeException("after exception");
  }

  @Test
  public void runtimeException() {
    throw new RuntimeException("runtimeException test");
  }

  @Test
  public void assertionFailed() {
    Assert.assertTrue("assertionFailed test", false);
  }
}
