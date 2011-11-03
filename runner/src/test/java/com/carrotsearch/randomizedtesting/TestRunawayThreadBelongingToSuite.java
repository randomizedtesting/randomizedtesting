package com.carrotsearch.randomizedtesting;

import java.util.concurrent.*;

import org.junit.*;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.annotations.ThreadLeaks;

public class TestRunawayThreadBelongingToSuite extends WithNestedTestClass {
  @ThreadLeaks(linger = 1000)
  public static class Nested extends RandomizedTest {
    private static ExecutorService executor;

    @BeforeClass
    public static void setup() {
      executor = Executors.newCachedThreadPool();
    }

    @AfterClass
    public static void cleanup() {
      executor.shutdown();
    }

    @Test @ThreadLeaks(leakedThreadsBelongToSuite = true)
    public void leaveBehind() throws Exception{
      assumeRunningNested();
      for (int i = 0; i < 3; i++) {
        executor.submit(new Runnable() {
          public void run() {
            RandomizedTest.sleep(100);
          }
        });
      }
    }
  }

  @Test
  public void leftOverThread() throws Throwable {
    Result r = JUnitCore.runClasses(Nested.class);
    Assert.assertEquals(0, r.getFailureCount());
  }    
}
