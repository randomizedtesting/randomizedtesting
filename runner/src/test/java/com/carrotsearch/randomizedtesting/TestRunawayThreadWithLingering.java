package com.carrotsearch.randomizedtesting;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.annotations.ThreadLeaks;

public class TestRunawayThreadWithLingering extends WithNestedTestClass {
  @ThreadLeaks(linger = 2000)
  public static class Nested extends RandomizedTest {
    @Test @ThreadLeaks(linger = 2000)
    public void leaveBehind() throws Exception{
      assumeRunningNested();
      Thread t = new Thread("lingering") {
        public void run() {
          RandomizedTest.sleep(250);
        }
      };
      t.start();
    }

    @Test
    public void leaveBehindClassLevel() throws Exception{
      leaveBehind();
    }

    @Test @ThreadLeaks(failTestIfLeaking = false)
    public void leaveBehindIgnore() throws Exception{
      leaveBehind();
    }
  }

  @Test
  public void leftOverThread() throws Throwable {
    Result r = JUnitCore.runClasses(Nested.class);
    Assert.assertEquals(0, r.getFailureCount());
  }    
}
