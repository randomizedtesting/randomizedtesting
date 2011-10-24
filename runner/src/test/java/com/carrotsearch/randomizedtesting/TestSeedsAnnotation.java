package com.carrotsearch.randomizedtesting;

import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.annotations.*;


/**
 * Check {@link Seeds}.
 */
public class TestSeedsAnnotation extends WithNestedTestClass {
  final static ArrayList<Long> seeds = new ArrayList<Long>();

  public static class Nested extends RandomizedTest {
    @Seeds({
      @Seed("deadbeef"),
      @Seed("cafebabe"),
      @Seed // Adds a randomized execution too.
    })
    @Test
    @Repeat(iterations = 2, useConstantSeed = true)
    public void testMe() {
      assumeRunningNested();
      seeds.add(getContext().getRandomness().getSeed());
    }
  }

  @Test
  public void checkSeeds() {
    HashMap<Long, Long> counts = new HashMap<Long, Long>();
    int N = 4;
    for (int i = 0; i < N; i++) {
      seeds.clear();
      Result result = JUnitCore.runClasses(Nested.class);
      Assert.assertEquals(3 * 2, result.getRunCount());
      Assert.assertEquals(0, result.getFailureCount());
      for (Long s : seeds) {
        if (!counts.containsKey(s))
          counts.put(s, 1L);
        else
          counts.put(s, counts.get(s) + 1);
      }
    }

    Assert.assertEquals(N * 2L, (long) counts.get(0xdeadbeefL));
    Assert.assertEquals(N * 2L, (long) counts.get(0xcafebabeL));
    counts.remove(0xdeadbeefL);
    counts.remove(0xcafebabeL);

    // Allow for a single collision.
    Assert.assertTrue(counts.size() >= N - 1);
  }
}
