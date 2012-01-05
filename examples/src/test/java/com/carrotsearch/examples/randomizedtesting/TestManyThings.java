package com.carrotsearch.examples.randomizedtesting;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Listeners;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Validators;
import com.carrotsearch.randomizedtesting.listeners.VerboseTestInfoPrinter;
import com.carrotsearch.randomizedtesting.validators.NoHookMethodShadowing;
import com.carrotsearch.randomizedtesting.validators.NoTestMethodOverrides;

/**
 * Shows multiple functional elements in one test case:
 * listeners, validators, hook methods...
 */
@Listeners({
    VerboseTestInfoPrinter.class
})
@Validators({
  NoHookMethodShadowing.class,
  NoTestMethodOverrides.class
})
public class TestManyThings extends RandomizedTest {
  @BeforeClass
  public static void setup() {
    info("before class");
  }

  @Before
  public void testSetup() {
    info("before test");
  }

  @Test
  public void alwaysFailing() {
    info("always failing");
    Assert.assertTrue(false);
  }

  @Repeat(iterations = 4)
  @Test
  public void halfFailing() {
    info("50% failing");
    Assert.assertTrue(randomBoolean());
  }

  @Repeat(iterations = 4)
  @Test
  public void halfAssumptionIgnored() {
    info("50% assumption ignored");
    Assume.assumeTrue(randomBoolean());
  }

  @Ignore
  @Test
  public void ignored() {
    info("ignored");
  }

  @After
  public void testCleanup() {
    info("after test");
  }

  @AfterClass
  public static void cleanup() {
    info("after class");
    try {
        throw new RuntimeException();
    } catch (Throwable t) {
        throw new Error("With message", t);
    }
  }

  private static void info(String msg) {
    System.out.println(msg + ", context: " + getContext().getRandomness());
  }
}

