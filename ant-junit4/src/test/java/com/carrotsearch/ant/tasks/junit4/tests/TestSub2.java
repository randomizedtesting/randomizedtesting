package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Test;

public class TestSub2 {
  @Test
  public void method1() {}
  
  @Test
  public void method2() {
    System.out.print("ab");
    System.out.flush();
    System.err.print("01");
    System.err.flush();
    System.out.print("cd");
    System.out.flush();
    System.err.print("23");
    System.err.flush();
  }
  
  @Test
  public void method3() {
    for (int i = 0; i < 10; i++) {
      System.out.println(i + " żółw(i)");
    }
  }
}
