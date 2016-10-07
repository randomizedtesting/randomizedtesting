package com.carrotsearch.randomizedtesting;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.annotations.Name;
import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Seed;
import com.carrotsearch.randomizedtesting.annotations.Seeds;

import static org.junit.Assert.*;

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
    
    @Seeds({@Seed("deadbeef"), @Seed("cafebabe")})
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
    Result result = runClasses(Nested.class);
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
      assumeRunningNested();
      return Arrays.asList($$($("xyz")));
    }
  }

  @Test
  public void testNameAnnotation() {
    Result result = runClasses(Nested2.class);
    Assert.assertEquals(1, result.getFailureCount());
    Assert.assertTrue(result.getFailures().get(0).getDescription().getMethodName().contains("paramName=xyz"));
    Assert.assertEquals("failing", RandomizedRunner.methodName(result.getFailures().get(0).getDescription()));
  }
  
  public static class Nested3 extends Nested2 {
    public Nested3(@Name("paramName") int value) {
      super(value);
    }

    @ParametersFactory
    public static Iterable<Object[]> parameters() {
      return Collections.emptyList();
    }
  }

  public static class Nested4 extends Nested3 {
    public Nested4(@Name("paramName") int value) {
      super(value);
    }

    @ParametersFactory
    public static Iterable<Object[]> parameters() {
      assumeTrue(false);
      throw new RuntimeException();
    }
  }

  @Test
  public void testEmptyParamsList() {
    Result result = runClasses(Nested3.class);
    Assert.assertEquals(0, result.getRunCount());
    Assert.assertEquals(0, result.getIgnoreCount());
    
    result = runClasses(Nested4.class);
    Assert.assertEquals(0, result.getRunCount());
    Assert.assertEquals(0, result.getIgnoreCount());    
  }  
}
