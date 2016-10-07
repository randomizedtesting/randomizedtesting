package com.carrotsearch.randomizedtesting;

import static org.junit.Assert.*;

import org.junit.*;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;

import com.carrotsearch.randomizedtesting.annotations.Seed;

/**
 * {@link RandomizedRunner} can augment stack traces to include seed info. Check
 * if it works.
 */
public class TestStackAugmentation extends WithNestedTestClass {
  @RunWith(RandomizedRunner.class)
  @Seed("deadbeef")
  public static class Nested {
    @Test @Seed("cafebabe")
    public void testMethod1() {
      assumeRunningNested();

      // Throws a chained exception.
      try {
        throw new RuntimeException("Inner.");
      } catch (Exception e) {
        throw new Error("Outer.", e);
      }
    }
  }

  @Test
  public void testMethodLevel() {
    Result result = runClasses(Nested.class);
    assertEquals(1, result.getFailureCount());

    Failure f = result.getFailures().get(0);
    String seedFromThrowable = RandomizedRunner.seedFromThrowable(f.getException());
    assertNotNull(seedFromThrowable);
    assertTrue("[DEADBEEF:CAFEBABE]".compareToIgnoreCase(seedFromThrowable) == 0);
  }

  @RunWith(RandomizedRunner.class)
  @Seed("deadbeef")
  public static class Nested2 {
    @BeforeClass
    public static void beforeClass() {
      assumeRunningNested();
      throw new Error("beforeclass.");
    }

    @Test @Seed("cafebabe")
    public void testMethod1() {
    }
  }

  @Test
  public void testBeforeClass() {
    Result result = runClasses(Nested2.class);
    assertEquals(1, result.getFailureCount());

    Failure f = result.getFailures().get(0);
    String seedFromThrowable = RandomizedRunner.seedFromThrowable(f.getException());
    assertNotNull(seedFromThrowable);
    assertTrue(f.getTrace(), "[DEADBEEF]".compareToIgnoreCase(seedFromThrowable) == 0);
  }
  
  @RunWith(RandomizedRunner.class)
  @Seed("deadbeef")
  public static class Nested3 {
    @AfterClass
    public static void afterClass() {
      assumeRunningNested();
      throw new Error("afterclass.");
    }

    @Test @Seed("cafebabe")
    public void testMethod1() {
    }
  }

  @Test
  public void testAfterClass() {
    Result result = runClasses(Nested3.class);
    assertEquals(1, result.getFailureCount());

    Failure f = result.getFailures().get(0);
    String seedFromThrowable = RandomizedRunner.seedFromThrowable(f.getException());
    assertNotNull(seedFromThrowable);
    assertTrue(f.getTrace(), "[DEADBEEF]".compareToIgnoreCase(seedFromThrowable) == 0);
  }  
}
