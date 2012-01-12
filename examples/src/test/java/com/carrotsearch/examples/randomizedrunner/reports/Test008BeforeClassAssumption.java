package com.carrotsearch.examples.randomizedrunner.reports;

import org.junit.*;

/** */
public class Test008BeforeClassAssumption {
  @BeforeClass
  public static void assumeMe() {
    Assume.assumeTrue(false);
  }

  @Test
  public void noop() {
  }
}
