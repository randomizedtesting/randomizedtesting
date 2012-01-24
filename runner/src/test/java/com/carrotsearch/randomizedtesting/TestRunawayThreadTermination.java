package com.carrotsearch.randomizedtesting;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import static com.carrotsearch.randomizedtesting.SysGlobals.*;

public class TestRunawayThreadTermination extends WithNestedTestClass {
  public static class Nested2 extends RandomizedTest {
    @Test
    public void leaveSpinningBehind() throws Exception{
      assumeRunningNested();
      Thread t = new Thread("spinning") {
        public void run() {
          while (true) {
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              // Ignore.
            }
          }
        }
      };
      t.start();
    }

    @Test
    public void leaveSleepingBehind() throws Exception{
      assumeRunningNested();
      Thread t = new Thread("sleeping") {
        public void run() {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            // fall through.
          }
        }
      };
      t.start();
    }    

    @Test
    public void leaveBulletProofBehind() throws Exception{
      assumeRunningNested();
      Thread t = new Thread("bulletproof") {
        public void run() {
          try {
            while (true) {
              Thread.sleep(1000);
            }
          } catch (Throwable t) {
            run(); // you can't kill me, I only grow longer.
          }
        }
      };
      t.start();
    }    
  }

  @Test
  public void leftOverThread() throws Throwable {
    System.setProperty(SYSPROP_KILLATTEMPTS, "3");
    System.setProperty(SYSPROP_KILLWAIT, "100");
    Result r = JUnitCore.runClasses(Nested2.class);
    Assert.assertEquals(3, r.getFailureCount());
  }    
}
