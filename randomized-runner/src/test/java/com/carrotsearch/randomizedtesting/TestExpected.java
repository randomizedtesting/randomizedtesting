package com.carrotsearch.randomizedtesting;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Test {@link Test#expected()}.
 */
public class TestExpected extends WithNestedTestClass {
  public static class Nested1 extends RandomizedTest {
    @Test(expected = RuntimeException.class)
    public void testMethod1() {
      throw new RuntimeException();
    }
    
    // We expect a RuntimeException but get an error: should fail.
    @Test(expected = RuntimeException.class)
    public void testMethod2() {
      assumeRunningNested();
      throw new Error();
    }
  }

  public static class Nested2 extends RandomizedTest {
    @Test(expected = RuntimeException.class)
    public void testMethod1() {
      assumeRunningNested();
      // Don't do anything.
    }
  }

  @Test
  public void testExpectedFailureDifferentException() {
    FullResult f = checkTestsOutput(2, 0, 1, 0, Nested1.class);
    Assertions.assertThat(f.getFailures().get(0).getException())
      .isInstanceOf(Error.class);
  }

  @Test
  public void testExpectedFailurePassed() {
    checkTestsOutput(1, 0, 1, 0, Nested2.class);
  }
}
