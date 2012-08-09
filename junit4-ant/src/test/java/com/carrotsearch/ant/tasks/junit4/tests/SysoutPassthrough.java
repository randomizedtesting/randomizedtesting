package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SysoutPassthrough {
  @BeforeClass
  public static void beforeclass() {
    println("-beforeclass-");
  }

  @Before
  public void before() {
    println("-before-");
  }

  @Test
  public void test1() {
    println("-test1-");
  }

  @Test
  public void test2() {
    println("-test2-");
  }

  @After
  public void after() {
    print("-after-");
  }

  @AfterClass
  public static void afterclass() {
    println("-afterclass-");
  }

  private static void print(String m) {
    System.out.print(m);
    System.out.flush();
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      throw new RuntimeException();
    }
  }

  private static void println(String m) {
    print(m + "\n");
  }
}
