package com.carrotsearch.randomizedtesting;

import java.util.Arrays;
import java.util.IdentityHashMap;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Seed;
import com.carrotsearch.randomizedtesting.annotations.Seeds;
import com.carrotsearch.randomizedtesting.annotations.TestCaseInstanceProvider;
import com.carrotsearch.randomizedtesting.annotations.TestCaseInstanceProvider.Type;

public class TestTestCaseInstanceProviders extends WithNestedTestClass {
  private static IdentityHashMap<Object,Object> set = new IdentityHashMap<>();

  @TestCaseInstanceProvider(Type.INSTANCE_PER_CONSTRUCTOR_ARGS)
  public static class Nested extends RandomizedTest {
    @Before
    public void setup() {
      assumeRunningNested();
      set.put(this, null);
    }
    
    @Test
    @Repeat(iterations = 3)
    public void testOne() {
      set.put(this, null);
    }

    @Test
    public void testTwo() {
      set.put(this, null);
    }
    
    @Seeds({@Seed("deadbeef"), @Seed("cafebabe")})
    @Test
    @Repeat(iterations = 2, useConstantSeed = true)
    public void testThree() {
      set.put(this, null);
    }
  }

  @Before
  public void setup() {
    set.clear();
  }

  @Test
  public void testDefaultConstructor() {
    Result result = runClasses(Nested.class);
    Assertions.assertThat(result.getFailureCount()).isEqualTo(0);
    Assertions.assertThat(result.getRunCount()).isEqualTo(3 + 1 + 2 * 2);
    Assertions.assertThat(set).hasSize(1);
  }

  public static class Nested2 extends Nested {
    public Nested2(String arg) {
    }

    @ParametersFactory
    public static Iterable<Object[]> params() {
      return Arrays.asList($("1"), $("2"), $("3"));
    }
  }
  
  @Test
  public void testParameterProviders() {
    Result result = runClasses(Nested2.class);
    Assertions.assertThat(set).hasSize(3);
    Assertions.assertThat(result.getFailureCount()).isEqualTo(0);
    Assertions.assertThat(result.getRunCount()).isEqualTo(3 * (3 + 1 + 2 * 2));
  }
}
