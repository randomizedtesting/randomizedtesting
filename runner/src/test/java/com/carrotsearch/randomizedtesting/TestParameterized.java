package com.carrotsearch.randomizedtesting;

import java.util.Arrays;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

public class TestParameterized extends RandomizedTest {
  private final int value;

  public TestParameterized(int value) {
    this.value = value;
  }
  
  @Test @Repeat(iterations = 5)
  public void testMe() {
    System.out.println(value);
  }

  @ParametersFactory
  public static Iterable<Object[]> parameters() {
    return Arrays.asList(
        new Object[] {1},
        new Object[] {2});
  }
}
