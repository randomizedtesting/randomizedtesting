package com.carrotsearch.randomizedtesting;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

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
    checkTestsOutput(16, 0, 0, 0, Nested.class);
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
    FullResult r = checkTestsOutput(1, 0, 1, 0, Nested2.class);
    Assertions.assertThat(r.getFailures()).hasSize(1);
    Assertions.assertThat(r.getFailures().get(0).getDescription().getMethodName())
      .contains("paramName=xyz");
    Assert.assertEquals("failing", RandomizedRunner.methodName(r.getFailures().get(0).getDescription()));
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


  public static class Nested5 extends RandomizedTest {
    public Nested5() {}

    @Test
    public void testMe() {}
    
    @ParametersFactory
    public static Iterable<Object[]> parameters() {
      return Arrays.asList(new Object[] {},
                           new Integer[] {});
    }
  }

  @Test
  public void testEmptyParamsList() {
    checkTestsOutput(0, 0, 0, 0, Nested3.class);
    checkTestsOutput(0, 0, 0, 0, Nested4.class);
  }

  @Test
  public void testNonObjectArray() {
    checkTestsOutput(2, 0, 0, 0, Nested5.class);
  }
}
