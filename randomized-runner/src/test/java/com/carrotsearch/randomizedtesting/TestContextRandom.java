package com.carrotsearch.randomizedtesting;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
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
      numbers.clear();
      for (int i = 0; i < 10; i++) {
        numbers.add(randomInt());
      }
    }
  }

  public static class Nested3 extends RandomizedTest {
    @Seed("deadbeef") // Fix the seed to get a repeatable result even if subthreads use randomness.
    @Test
    public void testMethod() throws Exception {
      Thread t = new Thread() {
        @Override
        public void run() {
          numbers.clear();
          for (int i = 0; i < 10; i++) {
            numbers.add(randomInt());
          }
        }
      };
      t.start();
      t.join();
    }
  }

  /**
   * Check that subthreads get the same randomness for {@link Seed}
   * annotation on a method.
   */
  @Test
  @Ignore("Forked threads get the master seed (by-design).")
  public void testFixedSeedSubthreads() {
    runTests(Nested3.class);
    List<Integer> run1 = new ArrayList<Integer>(numbers);
    runTests(Nested3.class);
    List<Integer> run2 = new ArrayList<Integer>(numbers);
    Assert.assertEquals(run1, run2);
  }

  @Test
  public void testFixedSeed() {
    runTests(Nested1.class);
    List<Integer> run1 = new ArrayList<Integer>(numbers);
    runTests(Nested1.class);
    List<Integer> run2 = new ArrayList<Integer>(numbers);
    Assert.assertEquals(run1, run2);
  }

  @Test
  public void testRandomSeed() {
    runTests(Nested2.class);
    List<Integer> run1 = new ArrayList<Integer>(numbers);
    runTests(Nested2.class);
    List<Integer> run2 = new ArrayList<Integer>(numbers);
    Assert.assertFalse(run1.equals(run2));
  }
}
