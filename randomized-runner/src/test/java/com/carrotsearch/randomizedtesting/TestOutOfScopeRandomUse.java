package com.carrotsearch.randomizedtesting;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Random;

import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.notification.Failure;

import com.carrotsearch.randomizedtesting.annotations.Timeout;

import org.junit.Assert;

/**
 * Check out of scope {@link Random} use.
 */
public class TestOutOfScopeRandomUse extends WithNestedTestClass {
  public static class Nested extends RandomizedTest {
    static Random instanceRandom;
    static Random beforeHookRandom;
    static Random staticContextRandom;
    volatile static Random otherThreadRandom;

    @BeforeClass
    public static void beforeClass() throws Exception {
      assumeRunningNested();
      instanceRandom = null;
      staticContextRandom = getRandom();
      
      // Should be able to use the random we've acquired for the static context.
      staticContextRandom.nextBoolean();
      
      Thread t = new Thread() {
        public void run() {
          otherThreadRandom = getRandom();
        }
      };
      t.start();
      t.join();
    }
    
    @AfterClass
    public static void afterClass() {
      if (!isRunningNested()) {
        return;
      }
      
      // Again should be able to use the random we've acquired for the static context.
      staticContextRandom.nextBoolean();
    }

    @Before
    public void before() {
      beforeHookRandom = getRandom();
    }

    private void touchRandom() {
      assumeRunningNested();

      // We shouldn't be able to reach to some other thread's random for which
      // the context is still valid.
      try {
        otherThreadRandom.nextBoolean();
        fail("Shouldn't be able to use another thread's Random.");
      } catch (IllegalStateException e) {
        // Expected.
      }

      // We should always be able to reach to @Before hook initialized Random.
      beforeHookRandom.nextBoolean();
      
      // Check if we're the first method or the latter methods.
      if (instanceRandom == null) {
        instanceRandom = getRandom();
      } else {
        // for anything not-first, we shouldn't be able to reuse first random anymore.
        try {
          instanceRandom.nextBoolean();
          fail("Shouldn't be able to use another test's Random.");
        } catch (IllegalStateException e) {
          // Expected.
        }
      }
    }

    @Test
    public void method1() throws Exception {
      touchRandom();
    }

    @Test @Timeout(millis = 2000)
    public void method2() throws Exception {
      touchRandom();
      
      // We shouldn't be able to use the static random because timeouting tests
      // are executed in their own thread and before and after class hooks are
      // dispatched in their own thread to allow termination/ interruptions.
      try {
        staticContextRandom.nextBoolean();
        fail("Shouldn't be able to use static context thread's Random.");
      } catch (IllegalStateException e) {
        // Expected.
      }      
    }    
  }

  @Before
  public void checkRunningWithAssertions() {
    // Sharing Random is only checked with -ea
    // https://github.com/randomizedtesting/randomizedtesting/issues/234
    RandomizedTest.assumeTrue("AssertionRandom not verifying sharing.", AssertingRandom.isVerifying());
  }
  
  @Test
  public void testCrossTestCaseIsolation() throws Throwable {
    List<Failure> failures = runTests(Nested.class).getFailures();
    Assertions.assertThat(failures).isEmpty();
  }

  @Test
  public void testCrossTestSuiteIsolation() {
    runTests(Nested.class);
    try {
      Nested.staticContextRandom.nextBoolean();
      Assert.fail("Shouldn't be able to use another suite's Random.");
    } catch (IllegalStateException e) {
      // Expected.
    }
  }
}
