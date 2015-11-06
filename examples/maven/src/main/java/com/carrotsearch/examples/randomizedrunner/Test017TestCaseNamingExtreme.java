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
 * When the test suite is using({@link Repeat}, {@link Seeds}
 * and {@link ParametersFactory}), it may happen that a single method
 * results in many actual tests. These tests have to be uniquely described
 * for JUnit (otherwise there is no way to tell which test case completed,
 * for example). The framework always attempts to add a unique suffix 
 * (repetition number) to test method's description. An extreme
 * example of this is shown in this class, which
 * repeats a single test with multiple seeds, repetitions and
 * two parameter factories which return the same argument multiple times.  
 */
@TestCaseOrdering(TestCaseOrdering.AlphabeticOrder.class)
public class Test017TestCaseNamingExtreme extends RandomizedTest {
  public String p;

  public Test017TestCaseNamingExtreme(@Name("param") String p) {
    this.p = p;
  }

  @Test
  @Repeat(iterations = 3, useConstantSeed = true)
  @Seeds({
    @Seed("00000001"),
    @Seed("00000002"),
    @Seed("00000003")
  })
  public void testMethod() {}

  @ParametersFactory
  public static Iterable<Object[]> factory1() {
    return Arrays.asList(
        $("p1"),
        $("p1"),
        $("p1"));
  }
  
  @ParametersFactory
  public static Iterable<Object[]> factory2() {
    return Arrays.asList(
        $("p1"),
        $("p1"),
        $("p1"));
  }  
}
