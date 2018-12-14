package com.carrotsearch.randomizedtesting.timeouts;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.SysGlobals;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;
import com.carrotsearch.randomizedtesting.annotations.*;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.notification.Failure;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Checks custom thread ignore policy.
 */
public class Test021SuiteTimeoutHanging extends WithNestedTestClass {
  private static AtomicBoolean stop;
  private static ArrayList<Thread> waitQueue = new ArrayList<>();

  @ThreadLeakScope(Scope.SUITE)
  @ThreadLeakLingering(linger = 0)
  @ThreadLeakAction({ThreadLeakAction.Action.WARN, ThreadLeakAction.Action.INTERRUPT})
  @ThreadLeakZombies(ThreadLeakZombies.Consequence.IGNORE_REMAINING_TESTS)
  @TimeoutSuite(millis = 1000)
  @TestCaseOrdering(TestCaseOrdering.AlphabeticOrder.class)
  public static class Nested1 extends RandomizedTest {
    @Test
    public void test001() throws Exception {
      synchronized (Thread.currentThread()) {
        waitQueue.add(Thread.currentThread());
        while (!stop.get()) {
          try {
            Thread.sleep(250);
          } catch (InterruptedException e) {
            // If interrupted, just continue. Don't release the lock.
            System.out.println("Interrupted.");
          }
          System.out.println("Still running.");
        }
      }
    }

    @Test
    public void test002() throws Exception {
      // Should not be executed.
      throw new Exception();
    }

    @BeforeClass
    private static void setup() {
      assumeRunningNested();
    }
  }

  @Test
  public void testThreadLeakInterruptsIsNotHangingOnJoin() throws Throwable {
    stop = new AtomicBoolean();

    System.setProperty(SysGlobals.SYSPROP_KILLATTEMPTS(), "1");
    AtomicReference<FullResult> result = new AtomicReference<>();
    Thread tester = new Thread(() -> {
      waitQueue.add(Thread.currentThread());
      result.set(runTests(Nested1.class));
    });
    tester.start();

    long deadline = System.currentTimeMillis() + 10000;
    while (System.currentTimeMillis() < deadline && tester.isAlive()) {
      Thread.sleep(250);
    }

    boolean testerAlive = tester.isAlive();

    // Wait for all threads to die.
    stop.set(true);
    for (Thread t : waitQueue) {
      t.join();
    }
    System.clearProperty(SysGlobals.SYSPROP_KILLATTEMPTS());

    // Make sure the tester was dead when we left the long wait loop. This
    // indicates the framework abandoned the thread it couldn't interrupt.
    Assertions.assertThat(testerAlive).isFalse();

    // Only one test executed.
    Assertions.assertThat(result.get().getRunCount()).isEqualTo(1);

    // Make sure suite timeouts have been reported.
    for (Failure failure : result.get().getFailures()) {
      Assertions.assertThat(failure.getMessage().toLowerCase(Locale.ROOT))
              .contains("suite timeout");
    }
  }
}
