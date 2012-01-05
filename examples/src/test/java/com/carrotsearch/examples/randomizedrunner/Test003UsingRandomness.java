package com.carrotsearch.examples.randomizedrunner;

import java.util.Random;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.*;

/**
 * So far we haven't really used the {@link Random} provided by
 * {@link RandomizedRunner}. The idea behind randomized tests is to, for each
 * test execution:
 * <ul>
 * <li>cover a possibly different execution path of the tested component,</li>
 * <li>cover a different data (input) passed to the tested component,
 * <li>
 * <li>execute in a different "environment" if there is environment variability.
 * </li>
 * </ul>
 * 
 * <p>
 * Let's see this on a simple example. Let's say we have a method that adds two
 * integers ({@link Adder#add(int, int)}). We can test this method using a "fixed" test
 * case as shown in {@link #fixedTesting} but this test will always execute in
 * an identical way (which is good if you're looking for regression coverage but
 * bad if you want to expand your tested domain).
 * 
 * <p>
 * A randomized test, on the other hand, will pick parameters from a larger
 * spectrum of values and assert on the method's contract. Here, we can make
 * sure the sum is always larger or equal than the arguments given two positive
 * integers. This assertion will fail quite often because of overflows as shown
 * in {@link #randomizedTesting()} (re-run the test a few times if it doesn't
 * fail the first time).
 */
public class Test003UsingRandomness extends RandomizedTest {
  @Test
  public void fixedTesting() {
    // Note how we use superclass methods, RandomizedTest extends from
    // Assert so these methods are readily available.
    assertEquals(4, Adder.add(2, 2));
    assertEquals(-1, Adder.add(0, -1));
    assertEquals(0, Adder.add(0, 0));
  }

  @Test
  public void randomizedTesting() {
    // Here we pick two positive integers. Note superclass utility methods.
    int a = randomIntBetween(0, Integer.MAX_VALUE);
    int b = randomIntBetween(0, Integer.MAX_VALUE);
    int result = Adder.add(a, b);
    assertTrue(result + " < (" + a + " or " + b + ")?", result >= a && result >= b);
  }
}
