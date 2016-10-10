package com.carrotsearch.randomizedtesting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;

public class TestParameterizedShufflesOrder extends WithNestedTestClass {
  static StringBuilder buf;

  public static abstract class Base extends RandomizedTest {
    private final int value;

    public Base(int value) {
      this.value = value;
    }

    @BeforeClass
    public static void checkNested() {
      assumeRunningNested();
    }

    @Test
    public void testFoo() {
      buf.append(value).append(",");
    }
  }

  public static class NoShuffle extends Base {
    public NoShuffle(int value) {
      super(value);
    }

    @ParametersFactory(shuffle = false)
    public static Iterable<Object[]> parameters() {
      List<Object[]> params = new ArrayList<Object[]>();
      for (int i = 0; i < 10; i++) {
        params.add($(i));
      }
      return params;
    }
  }

  public static class WithShuffle extends Base {
    public WithShuffle(int value) {
      super(value);
    }

    @ParametersFactory()
    public static Iterable<Object[]> parameters() {
      List<Object[]> params = new ArrayList<Object[]>();
      for (int i = 0; i < 10; i++) {
        params.add($(i));
      }
      return params;
    }
  }
  
  @Test
  public void testWithoutShuffle() {
    Set<String> runs = new HashSet<String>();
    for (int i = 0; i < 10; i++) {
      buf = new StringBuilder();
      Assertions.assertThat(runTests(NoShuffle.class).wasSuccessful()).isTrue();
      runs.add(buf.toString());
    }
    Assertions.assertThat(runs).hasSize(1);
  }
  
  @Test
  public void testWithShuffle() {
    Set<String> runs = new HashSet<String>();
    int iters = 10;
    for (int i = 0; i < iters; i++) {
      buf = new StringBuilder();
      Assertions.assertThat(runTests(WithShuffle.class).wasSuccessful()).isTrue();
      runs.add(buf.toString());
    }
    Assertions.assertThat(runs).hasSize(iters);
  }  
}
