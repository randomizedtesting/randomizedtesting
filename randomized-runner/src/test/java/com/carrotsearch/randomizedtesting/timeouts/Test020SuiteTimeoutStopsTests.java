package com.carrotsearch.randomizedtesting.timeouts;

import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakLingering;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;
import com.carrotsearch.randomizedtesting.annotations.TimeoutSuite;

/**
 * Checks custom thread ignore policy.
 */
public class Test020SuiteTimeoutStopsTests extends WithNestedTestClass {
  @ThreadLeakScope(Scope.SUITE)
  @TimeoutSuite(millis = 500)
  @ThreadLeakLingering(linger = 0)
  public static class Nested1 extends RandomizedTest {
    @Test
    public void test001() { idle("1"); }
    @Test
    public void test002() { idle("2"); }
    @Test
    public void test003() { idle("3"); }
    @Test
    public void test004() { idle("4"); }
    @Test
    public void test005() { idle("5"); }
    @Test
    public void test006() { idle("6"); }

    private static AtomicInteger executedTests;

    @BeforeClass
    private static void setup() {
      assumeRunningNested();
      executedTests = new AtomicInteger();
    }

    private void idle(String m) {
      if (executedTests.getAndIncrement() == 0) {
        System.out.println("before timeout(" + m + ")");
        try {
          while (true) { Thread.sleep(1000); }
        } catch (InterruptedException e) {
          System.out.println("suite timeout(" + m + ")");
          // Suite timeout.
          return;
        }
      } else {
        System.out.println("after timeout(" + m + ")");
      }
    }
  }

  @Test
  public void testExceptionInFilter() throws Throwable {
    runTests(Nested1.class);
    Assertions.assertThat(getSysouts()).doesNotContain("after timeout");
    sysout.println(getLoggingMessages());
  }    
}
