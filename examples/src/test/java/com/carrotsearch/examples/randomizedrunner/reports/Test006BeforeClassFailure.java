package com.carrotsearch.examples.randomizedrunner.reports;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

/** */
public class Test006BeforeClassFailure {
  @BeforeClass
  public static void failOnMe() {
    Assert.assertTrue(false);
  }

  @Test
  public void noop() {
  }
}
