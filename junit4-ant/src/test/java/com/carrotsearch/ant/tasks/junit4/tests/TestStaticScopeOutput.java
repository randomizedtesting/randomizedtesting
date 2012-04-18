package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Static scope output.
 */
public class TestStaticScopeOutput {
  static {
    System.out.println("static-scope");
    System.out.flush();
  }

  @BeforeClass
  public static void beforeClass() {
    System.out.println("before-class");
    System.out.flush();
  }
  
  @Test
  public void testMethod() {
    System.out.println("test-method");
    System.out.flush();
  }

  @AfterClass
  public static void afterClass() {
    System.out.println("after-class");
    System.out.flush();
  }  
}
