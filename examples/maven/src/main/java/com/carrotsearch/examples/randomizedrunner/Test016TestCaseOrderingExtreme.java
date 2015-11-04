package com.carrotsearch.examples.randomizedrunner;

import java.util.Arrays;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Name;
import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Seed;
import com.carrotsearch.randomizedtesting.annotations.Seeds;
import com.carrotsearch.randomizedtesting.annotations.TestCaseOrdering;

/**
 * Typically you will <b>not</b> want to order test cases and leave them to be
 * randomly shuffled by the runner (this prevents any accidental test order
 * dependencies). Sometimes, however, it is useful to have tests run in an
 * predefined order. 
 * 
 * This example demonstrates how to order tests using a custom callback. The
 * example is a bit extreme in that it uses all kinds of modifiers that
 * multiply a single test method's execution ({@link Repeat}, {@link Seeds},
 * {@link ParametersFactory}). 
 */
@TestCaseOrdering(TestCaseOrdering.AlphabeticOrder.class)
public class Test016TestCaseOrderingExtreme extends RandomizedTest {
  private String p;

  public Test016TestCaseOrderingExtreme(@Name("p") String p) {
    this.p = p;
  }

  @Test
  public void testA() {}

  @Test
  @Repeat(iterations = 3)
  public void testB() {}

  @Test
  @Repeat(iterations = 3, useConstantSeed = true)
  @Seeds({
    @Seed("00000001"),
    @Seed("00000002"),
    @Seed("00000003")
  })
  public void testC() {}

  @ParametersFactory
  public static Iterable<Object[]> parameters() {
    return Arrays.asList(
        $("p1"),
        $("p3"),
        $("p2"));
  }
}
