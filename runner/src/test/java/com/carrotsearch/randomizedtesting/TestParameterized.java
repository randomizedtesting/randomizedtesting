package com.carrotsearch.randomizedtesting;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.annotations.Name;
import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Seed;
import com.carrotsearch.randomizedtesting.annotations.Seeds;

public class TestParameterized extends WithNestedTestClass {
  public static class Nested extends RandomizedTest {
    public Nested(@Name("value") int value, @Name("string") String v) {
    }

    @Test
    @Repeat(iterations = 3)
    public void testOne() {
    }
    
    @Test
    public void testTwo() {
    }
    
    @Seeds({@Seed("deadbeef"), @Seed("cafebabe"),})
    @Test
    @Repeat(iterations = 2, useConstantSeed = true)
    public void testThree() {
    }
    
    @ParametersFactory
    public static Iterable<Object[]> parameters() {
      return Arrays.asList($$(
          $(1, "abc"),
          $(2, "def")));
    }
  }

  @Test
  public void testWithRepeatsAndSeeds() {
    Result result = JUnitCore.runClasses(Nested.class);
    Assert.assertEquals(16, result.getRunCount());
  }

  public static class Nested2 extends RandomizedTest {
    public Nested2(@Name("paramName") int value) {
    }

    @Test
    public void failing() {
      assumeRunningNested();
      fail();
    }
    
    @ParametersFactory
    public static Iterable<Object[]> parameters() {
      return Arrays.asList($$($("xyz")));
    }
  }

  @Test
  public void testNameAnnotation() {
    Result result = JUnitCore.runClasses(Nested2.class);
    Assert.assertEquals(1, result.getFailureCount());
    Assert.assertTrue(result.getFailures().get(0).getDescription().getMethodName().contains("paramName=xyz"));
  }
}
