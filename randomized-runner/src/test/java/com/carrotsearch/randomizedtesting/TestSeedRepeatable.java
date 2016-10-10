package com.carrotsearch.randomizedtesting;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.annotations.Seed;

public class TestSeedRepeatable extends WithNestedTestClass {
  @Seed("deadbeef")
  @RunWith(RandomizedRunner.class)
  public static class Nested {
    static Map<String, Object> seeds = new HashMap<String, Object>();

    @BeforeClass
    public static void nested() {
      assumeRunningNested();
    }
    
    @Test
    public void testMethod1() {
      checkSeed("method1");
    }

    @Test
    public void testMethod2() {
      checkSeed("method2");
    }
    
    private void checkSeed(String key) {
      final Object seed = RandomizedContext.current().getRandomness().getSeed();
      if (seeds.containsKey(key)) {
        assertEquals(seeds.get(key), seed);
      } else {
        seeds.put(key, seed);
      }
    }    
  }
  
  /**
   * Check if methods get the same seed on every run with a fixed runner's seed.
   */
  @Test
  public void testSameMethodRandomnessWithFixedRunner() {
    Nested.seeds.clear();
    checkTestsOutput(2, 0, 0, 0, Nested.class);
    checkTestsOutput(2, 0, 0, 0, Nested.class);
    checkTestsOutput(2, 0, 0, 0, Nested.class);
  }  
}
