package com.carrotsearch.randomizedtesting;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import com.carrotsearch.randomizedtesting.annotations.Seed;

/**
 * Check if the context's random is indeed repeatable.
 */
public class TestContextRandom extends WithNestedTestClass {
  static ArrayList<Integer> numbers = new ArrayList<Integer>();

  public static class Nested1 extends RandomizedTest {
    @Seed("deadbeef") // Fix the seed to get a repeatable result
    @Test
    public void testMethod() {
      numbers.clear();
      for (int i = 0; i < 10; i++) {
        numbers.add(randomInt());
      }
    }
  }

  public static class Nested2 extends RandomizedTest {
    @Test
    public void testMethod() {
      for (int i = 0; i < 10; i++) {
        numbers.clear();
        numbers.add(randomInt());
      }
    }
  }

  @Test
  public void testFixedSeed() {
    JUnitCore.runClasses(Nested1.class);
    List<Integer> run1 = new ArrayList<Integer>(numbers);
    JUnitCore.runClasses(Nested1.class);
    List<Integer> run2 = new ArrayList<Integer>(numbers);
    Assert.assertEquals(run1, run2);
  }

  @Test
  public void testRandomSeed() {
    JUnitCore.runClasses(Nested2.class);
    List<Integer> run1 = new ArrayList<Integer>(numbers);
    JUnitCore.runClasses(Nested2.class);
    List<Integer> run2 = new ArrayList<Integer>(numbers);
    Assert.assertFalse(run1.equals(run2));
  }
}
