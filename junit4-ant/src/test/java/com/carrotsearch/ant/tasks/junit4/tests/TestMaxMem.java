package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Assert;
import org.junit.Test;

public class TestMaxMem {
  /**
   * Check max memory setting to be less than 100mb.
   */
  @Test
  public void testMaxMemory() {
    long maxMemory = Runtime.getRuntime().maxMemory();
    Assert.assertTrue(maxMemory / (1024 * 1024) < 110);
  }
}
