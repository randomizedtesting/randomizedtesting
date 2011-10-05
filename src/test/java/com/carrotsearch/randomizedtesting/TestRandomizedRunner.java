package com.carrotsearch.randomizedtesting;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

/**
 * Tests {@link RandomizedRunner}
 */
public class TestRandomizedRunner {
  public static class Sub {
  }

  @RunWith(RandomizedRunner.class)
  public static class SubSub {
    @Test
    public int methodA() {
      return 0;
    }
    
    @Test
    public void methodB() {
    }    
  }

  @Test
  public void beforesCalled() {
    Result result = JUnitCore.runClasses(SubSub.class);
    System.out.println("Run count: " + result.getRunCount());
  }
}
