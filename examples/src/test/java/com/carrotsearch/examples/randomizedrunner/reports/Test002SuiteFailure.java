package com.carrotsearch.examples.randomizedrunner.reports;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Suite-level failures.
 */
public class Test002SuiteFailure {
  @BeforeClass
  public static void beforeClass() {
    throw new RuntimeException("beforeClass");
  }

  @Test
  public void testCase() {}

  @AfterClass
  public static void afterClass() {
    throw new RuntimeException("afterClass");
  }
}
