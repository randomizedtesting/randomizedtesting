package com.carrotsearch.randomizedtesting;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;

import com.carrotsearch.randomizedtesting.rules.SystemPropertiesInvariantRule;

public class WithNestedTestClass {
  private static boolean runningNested;

  @Rule
  public static SystemPropertiesInvariantRule noLeftOverProperties =
    new SystemPropertiesInvariantRule();

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
