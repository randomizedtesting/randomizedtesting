package com.carrotsearch.randomizedtesting;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;

public class WithNestedTestClass {
  private static boolean runningNested;

  @BeforeClass
  public static final void setupNested() {
    runningNested = true;
  }
  
  
  @AfterClass
  public static final void clearNested() {
    runningNested = false;
  }
  
  protected static boolean isRunningNested() {
    return runningNested;
  }
  
  protected static void assumeRunningNested() {
    Assume.assumeTrue(runningNested);
  }
}
