package com.carrotsearch.randomizedtesting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.RandomizedContext;
import com.carrotsearch.randomizedtesting.RandomizedRunner;

import static org.junit.Assert.*;

/**
 * Seed fixing for static fixtures and/or methods using system properties.
 */
public class TestSeedFixingWithProperties extends WithNestedTestClass {
  static List<Long> seeds = new ArrayList<Long>();

  @RunWith(RandomizedRunner.class)
  public static class ExpectSeed {
    @BeforeClass
    public static void staticFixture() {
      seeds.add(RandomizedContext.current().getRandomness().getSeed());
    }

    @Test
    public void testMethod() {
      seeds.add(RandomizedContext.current().getRandomness().getSeed());
    }
  }

  /**
   * Combined seed: fixing everything: the runner, method and any repetitions.
   */
  @Test
  public void testRunnerAndMethodProperty() {
    System.setProperty(RandomizedRunner.SYSPROP_RANDOM_SEED, "deadbeef:cafebabe");
    System.setProperty(RandomizedRunner.SYSPROP_ITERATIONS, "3");
    JUnitCore.runClasses(ExpectSeed.class);
    assertEquals(Arrays.asList(0xdeadbeefL, 0xcafebabeL, 0xcafebabeL, 0xcafebabeL), seeds);
  }

  /**
   * Runner seed fixing only (methods have predictable pseudo-random seeds derived from 
   * the runner). 
   */
  @Test
  public void testFixedRunnerPropertyOnly() {
    System.setProperty(RandomizedRunner.SYSPROP_RANDOM_SEED, "deadbeef");
    System.setProperty(RandomizedRunner.SYSPROP_ITERATIONS, "3");
    Result result = JUnitCore.runClasses(ExpectSeed.class);
    assertEquals(3, result.getRunCount());
    assertEquals(0xdeadbeefL, seeds.get(0).longValue());
    // _very_ slim chances of this actually being true...
    assertFalse(
        seeds.get(0).longValue() == seeds.get(1).longValue() &&
        seeds.get(1).longValue() == seeds.get(2).longValue());

    // We should have the same randomized seeds on methods for the same runner seed,
    // so check if this is indeed true.
    List<Long> copy = new ArrayList<Long>(seeds);
    seeds.clear();
    JUnitCore.runClasses(ExpectSeed.class);
    assertEquals(copy, seeds);
  }

  @Before
  public void cleanupBefore() {
    cleanupAfter();
  }
  
  @After
  public void cleanupAfter() {
    System.clearProperty(RandomizedRunner.SYSPROP_ITERATIONS);
    System.clearProperty(RandomizedRunner.SYSPROP_RANDOM_SEED);
    seeds.clear();
  }
}
