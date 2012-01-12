package com.carrotsearch.examples.randomizedrunner.reports;

import org.junit.*;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.RandomizedRunner;

/** */
@RunWith(RandomizedRunner.class)
public class Test008BeforeClassAssumptionRR {
  @BeforeClass
  public static void assumeMe() {
    Assume.assumeTrue(false);
  }

  @Test
  public void noop() {
  }
}
