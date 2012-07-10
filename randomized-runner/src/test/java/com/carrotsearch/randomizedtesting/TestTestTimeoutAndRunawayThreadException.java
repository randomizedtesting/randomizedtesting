package com.carrotsearch.randomizedtesting;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.carrotsearch.randomizedtesting.annotations.Timeout;
import com.carrotsearch.randomizedtesting.rules.SystemPropertiesRestoreRule;

@Ignore("https://github.com/carrotsearch/randomizedtesting/issues/115")
public class TestTestTimeoutAndRunawayThreadException extends WithNestedTestClass {
  private static String LEAKED_THREAD_MSG = "A thread from a timed out test was alive!";

  public static class Nested extends RandomizedTest {
    static Thread t; 

    @Test
    @Timeout(millis = 500)
    public void timeoutQuickly() throws Exception{
      assumeRunningNested();
      t = new Thread("spinning") {
        public void run() {
          while (true) {
            try {
              Thread.sleep(10000);
            } catch (InterruptedException e) {
              throw new RuntimeException("foobar");
            }
          }
        }
      };
      t.start();

      while (true) {
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
      }
    }
    
    @AfterClass
    public static void checkLeakedThread() {
      if (t != null && t.isAlive()) {
        throw new RuntimeException(LEAKED_THREAD_MSG);
      }
    }
  }

  @Rule
  public SystemPropertiesRestoreRule restoreProperties = 
    new SystemPropertiesRestoreRule();

  @Test
  public void leftOverThread() throws Throwable {
    Result r = JUnitCore.runClasses(Nested.class);

    System.clearProperty(SysGlobals.SYSPROP_KILLATTEMPTS());
    System.clearProperty(SysGlobals.SYSPROP_KILLWAIT());

    int afterTermination = 0;
    int leakedThreadMsg = 0;
    for (Failure f : r.getFailures()) {
      if (f.getTrace().contains("after termination attempt")) {
        afterTermination++;
      }
      if (f.getTrace().contains(LEAKED_THREAD_MSG)) {
        leakedThreadMsg++;
      }
    }

    // The thread should have been terminated with an "after-timeout" attempt.
    Assert.assertEquals(1, afterTermination);
    // The leaked thread should not be alive.
    Assert.assertEquals(0, leakedThreadMsg);
  }    
}
