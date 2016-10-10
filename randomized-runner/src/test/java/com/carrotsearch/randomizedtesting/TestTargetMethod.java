package com.carrotsearch.randomizedtesting;

import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.annotations.Repeat;

public class TestTargetMethod extends WithNestedTestClass {
  public static class Nested extends RandomizedTest {
    @Before
    public void checkInHook() {
      assumeRunningNested();
    }

    @Test
    @Repeat(iterations = 3)
    public void testOne() {
      Assertions.assertThat(RandomizedContext.current().getTargetMethod().getName())
        .isEqualTo("testOne");
    }
    
    @Test
    public void testTwo() {
      Assertions.assertThat(RandomizedContext.current().getTargetMethod().getName())
        .isEqualTo("testTwo");
    }
    
    @AfterClass
    @BeforeClass
    public static void staticHooks() {
      Assertions.assertThat(RandomizedContext.current().getTargetMethod())
        .isNull();
    }
  }

  @Test
  public void testTargetMethodAvailable() {
    checkTestsOutput(4, 0, 0, 0, Nested.class);
  }
}
