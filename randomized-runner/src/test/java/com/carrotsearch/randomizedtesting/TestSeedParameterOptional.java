package com.carrotsearch.randomizedtesting;

import java.util.HashSet;

import org.junit.Assert;

import org.junit.Test;
import org.junit.runner.*;
import org.junit.runner.notification.RunListener;

import com.carrotsearch.randomizedtesting.annotations.*;


/**
 * Check if <code>seed</code> parameter is optional if no repetitions
 * of the test are requested.  
 */
public class TestSeedParameterOptional extends WithNestedTestClass {
  public static class Nested extends RandomizedTest {
    @Seeds({
      @Seed("deadbeef"),
      @Seed("cafebabe"),
      @Seed // Adds a randomized execution too.
    })
    @Test
    @Repeat(iterations = 2, useConstantSeed = true)
    public void method1() { }

    @Seed("cafebabe")
    @Test
    public void method2() { }
    
    @Test
    public void method3() { }        
  }
  
  @Test
  public void checkNames() {
    final HashSet<String> tests = new HashSet<String>();

    JUnitCore junit = new JUnitCore();
    junit.addListener(new RunListener() {
      @Override
      public void testStarted(Description description) throws Exception {
        tests.add(description.getMethodName());
      }
    });

    junit.run(Nested.class);
    
    // Single repetitions, no seed parameter in test name.
    Assert.assertTrue(tests.contains("method2"));
    Assert.assertTrue(tests.contains("method3"));

    // Method 1 has 2x3 repetitions.
    int count = 0;
    for (String s : tests) {
      if (s.startsWith("method1")) {
        count++;
      }
    }
    Assert.assertEquals(6, count);
  }
}
