package com.carrotsearch.randomizedtesting;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.carrotsearch.randomizedtesting.annotations.Timeout;

public class TestTestTimeoutAndRunawayThreadException extends WithNestedTestClass {
  public static class Nested extends RandomizedTest {
    @Test
    @Timeout(millis = 500)
    public void timeoutQuickly() throws Exception{
      assumeRunningNested();
      Thread t = new Thread("spinning") {
        public void run() {
          while (true) {
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              throw new RuntimeException("foobar");
            }
          }
        }
      };
      t.start();
      Thread.sleep(5000);
    }
  }

  @Test
  public void leftOverThread() throws Throwable {
    Result r = JUnitCore.runClasses(Nested.class);
    
    int afterTermination = 0;
    for (Failure f : r.getFailures()) {
      if (f.getTrace().contains("after termination attempt")) {
        afterTermination++;
      }
    }
    
    Assert.assertEquals(1, afterTermination);
  }    
}
