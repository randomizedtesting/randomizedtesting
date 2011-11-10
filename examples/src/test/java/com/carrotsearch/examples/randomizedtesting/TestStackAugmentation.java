package com.carrotsearch.examples.randomizedtesting;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;

import com.carrotsearch.randomizedtesting.RandomizedRunner;

/**
 * {@link RandomizedRunner} can augment stack traces to include seed info. This
 * is an example of how it works (dumps the seed of the failed method to the
 * console).
 */
public class TestStackAugmentation {
  @RunWith(RandomizedRunner.class)
  public static class Nested {
    @Test
    public void testMethod1() {
      // Throws a chained exception.
      try {
        throw new RuntimeException("Inner.");
      } catch (Exception e) {
        throw new Error("Outer.", e);
      }
    }
  }

  @Test
  public void testSameMethodRandomnessWithFixedRunner() {
    Result result = JUnitCore.runClasses(Nested.class);
    assertEquals(1, result.getFailureCount());
    
    Failure f = result.getFailures().get(0);
    System.out.println("Partial stack trace from failed randomized method: ");
    for (String s : Arrays.asList(f.getTrace().split("\n")).subList(0, 5)) {
      System.out.println(s);
    }
  }
}
