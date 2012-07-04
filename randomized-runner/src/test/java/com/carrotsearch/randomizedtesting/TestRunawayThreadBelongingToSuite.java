package com.carrotsearch.randomizedtesting;

import java.util.concurrent.*;

import org.junit.*;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeaks;
import com.carrotsearch.randomizedtesting.rules.SystemPropertiesRestoreRule;

import static com.carrotsearch.randomizedtesting.SysGlobals.*;

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
  
  @ThreadLeaks(linger = 1000, leakedThreadsBelongToSuite = true)
  public static class Nested2 extends RandomizedTest {
    @Test @ThreadLeaks(leakedThreadsBelongToSuite = true)
    public void leaveBehind() throws Exception{
      assumeRunningNested();
      Thread t = new Thread() {
        public void run() {
            while (true) {
              try {
                RandomizedTest.sleep(1000);
              } catch (Throwable t) {
                // ignore.
              }
            }
        }
      };
      t.setDaemon(true);
      t.start();
    }
  }
  
  @Rule
  public SystemPropertiesRestoreRule restoreProps = new SystemPropertiesRestoreRule(); 

  @Test
  public void leftOverZombie() throws Throwable {
    System.setProperty(SYSPROP_KILLWAIT(), "100");
    Result r = JUnitCore.runClasses(Nested2.class);

    Assert.assertEquals(1, r.getFailureCount());
  }
}
