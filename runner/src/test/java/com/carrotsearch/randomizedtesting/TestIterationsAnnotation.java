package com.carrotsearch.randomizedtesting;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.annotations.Repeat;

/**
 * Nightly mode checks.
 */
public class TestIterationsAnnotation extends RandomizedTest {
  static int iterations = 0;

  @Test @Repeat(iterations = 10)
  public void nightly() {
    iterations++;
  }

  @BeforeClass
  public static void clean() {
    iterations = 0;
  }
  
  @AfterClass
  public static void cleanupAfter() {
    Assert.assertEquals(10, iterations);
  }
}
