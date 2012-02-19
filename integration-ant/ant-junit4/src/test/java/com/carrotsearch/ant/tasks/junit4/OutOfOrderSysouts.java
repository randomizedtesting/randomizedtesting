package com.carrotsearch.ant.tasks.junit4;

import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;


public class OutOfOrderSysouts extends RandomizedTest {
  final static CountDownLatch latch = new CountDownLatch(1);
  static {
    new Thread("background-non-daemon") {
      public void run() {
        sysout("Starting...");
        try {
          while (true) {
            syserr("garbage...");
            Thread.yield();
          }
        } catch (Exception e) {
          // empty.
        }
      }
    }.start();
  }

  @Before
  public void before() {
    latch.countDown();
    sleep(1 + randomInt(20));
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
