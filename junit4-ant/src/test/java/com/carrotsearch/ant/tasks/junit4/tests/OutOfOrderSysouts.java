package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.*;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;

@ThreadLeakScope(Scope.SUITE)
public class OutOfOrderSysouts extends RandomizedTest {
  private static Thread t;

  static {
    t = new Thread("background-non-daemon") {
      public void run() {
        sysout("Starting...");
        try {
          while (true) {
            syserr("garbage...");
            Thread.sleep(1);
          }
        } catch (Exception e) {
          // empty.
        }
      }
    };
    t.start();
  }

  @Before
  public void before() {
    sleep(1 + randomIntBetween(0, 20));
  }
  
  @AfterClass
  public static void afterClass() throws Exception {
    t.interrupt();
    t.join();
  }

  @Test public void method1() { sysout("method1"); }
  @Test public void method2() { sysout("method2"); }
  @Test public void method3() { sysout("method3"); }
  @Test public void method4() { sysout("method4"); }
  @Test public void method5() { sysout("method5"); }
  @Test public void method6() { sysout("method6"); }
  @Test public void method7() { sysout("method7"); }
  @Test public void method8() { sysout("method8"); }
  @Test public void method9() { sysout("method9"); }
  @Test public void method10() { sysout("method10"); }
  @Test public void method11() { sysout("method11"); }

  static void sysout(String msg) {
    System.out.println(msg);
    System.out.flush();
  }
  
  static void syserr(String msg) {
    System.out.println(msg);
    System.out.flush();
  }  
}
