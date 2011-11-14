package com.carrotsearch.randomizedtesting;

import java.util.Arrays;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Seed;
import com.carrotsearch.randomizedtesting.annotations.Seeds;

public class TestParameterized extends RandomizedTest {
  private final int value;
  private final String string;

  public TestParameterized(int value, String v) {
    this.value = value;
    this.string = v;
  }

  @Test @Repeat(iterations = 3)
  public void testOne() {
    System.out.println(value);
  }

  @Test
  public void testTwo() {
    System.out.println(value);
  }

  @Seeds({
    @Seed("deadbeef"),
    @Seed("cafebabe"),
  })
  @Test @Repeat(iterations = 2, useConstantSeed = true)
  public void testThree() {
    System.out.println(value);
  }

  @ParametersFactory
  public static Iterable<Object[]> parameters() {
    return Arrays.asList(new Object[][] {
        new Object[] {1, "abc"},
        new Object[] {2, "def"}
    });
  }
}
