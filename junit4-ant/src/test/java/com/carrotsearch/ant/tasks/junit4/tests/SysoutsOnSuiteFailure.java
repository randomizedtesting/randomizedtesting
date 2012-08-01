package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

public class SysoutsOnSuiteFailure {
  
  @BeforeClass
  public static void beforeClass() {
    System.out.println("beforeclass-sysout.");
  }

  @Test
  public void assumptionIgnored() {
    System.out.println("ignored-sysout.");
    Assume.assumeTrue(false);
  }

  @Test
  public void success() {
    System.out.println("success-sysout.");
  }  
  
  @AfterClass
  public static void afterClass() {
    System.out.println("afterclass-sysout.");
    throw new RuntimeException();
  }
}
