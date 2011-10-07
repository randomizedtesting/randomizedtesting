package com.carrotsearch.randomizedtesting.examples;

import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Nightly;

/**
 * {@link RandomizedTest} is a scaffolding class for faster setup of a randomized
 * test case.
 */
public class TestExtendingRandomizedTest extends RandomizedTest {
  @BeforeClass
  public static void ensureRunsRandomized() {
    assertNotNull(getContext());
  }

  @Test
  public void failOrNot() {
    assertTrue(randomBoolean());
  }

  @Test @Nightly("Takes about 2 seconds to run.")
  public void runNightlyOnly() throws Exception {
    Thread.sleep(2000);
  }
}
