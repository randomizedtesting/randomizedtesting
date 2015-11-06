package com.carrotsearch.examples.randomizedrunner;

import java.util.Arrays;
import java.util.Comparator;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.TestMethodAndParams;
import com.carrotsearch.randomizedtesting.annotations.Name;
import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import com.carrotsearch.randomizedtesting.annotations.TestCaseOrdering;

/**
 * Typically you will <b>not</b> want to order test cases and leave them to be
 * randomly shuffled by the runner (this prevents any accidental test order
 * dependencies). Sometimes, however, it is useful to have tests run in an
 * predefined order. 
 * 
 * This example demonstrates how to order tests using a custom callback. The
 * example uses {@link ParametersFactory} to demonstrate how this can be useful
 * with custom arguments. 
 */
@TestCaseOrdering(Test016CustomTestCaseOrdering.CustomOrder.class)
public class Test016CustomTestCaseOrdering extends RandomizedTest {
  public static class CustomOrder implements Comparator<TestMethodAndParams> {
    public int compare(TestMethodAndParams o1, TestMethodAndParams o2) {
      // Order by test method name first,
      int v = o1.getTestMethod().getName().compareTo(
              o2.getTestMethod().getName());
      // Secondary order: by constructor argument.
      if (v == 0) {
        v = ((String) o1.getInstanceArguments().get(0)).compareTo(
            ((String) o2.getInstanceArguments().get(0)));
      }
      return v;
    }
  }
  
  public String p;

  public Test016CustomTestCaseOrdering(@Name("p") String p) {
    this.p = p;
  }

  @Test
  public void testA() {
  }

  @Test
  public void testB() {
  }

  @ParametersFactory
  public static Iterable<Object[]> factory1() {
    return Arrays.asList(
        $("1"),
        $("2"),
        $("3"));
  }
}
