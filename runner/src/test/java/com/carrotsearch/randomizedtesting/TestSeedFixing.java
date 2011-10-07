package com.carrotsearch.randomizedtesting;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.annotations.Seed;

/**
 * Seed fixing for static fixtures and/or methods using annotations.
 */
@RunWith(RandomizedRunner.class)
@Seed("deadbeef")
public class TestSeedFixing extends WithNestedTestClass {
  @BeforeClass
  public static void beforeClass() {
    assertEquals(0xdeadbeefL, RandomizedContext.current().getRandomness().getSeed());
  }

  @Seed("cafebabe")
  @Test
  public void dummy() {
    assertEquals(0xcafebabeL, RandomizedContext.current().getRandomness().getSeed());
  }
}
