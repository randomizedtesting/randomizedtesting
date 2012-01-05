package com.carrotsearch.examples.randomizedrunner;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.RandomizedRunner;

/**
 * Shows that multiple thrown exceptions are reported individually
 * in JUnit runners in IDEs/ batch runners like ANT.
 * 
 * <p>Exceptions are not chained or suppressed, they are all reported
 * back.
 */
@RunWith(RandomizedRunner.class)
public class TestNoSuppressedException {
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
