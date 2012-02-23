package com.carrotsearch.randomizedtesting;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.annotations.TestMethodProviders;

public class TestOverlappingMethodProviders {
  @TestMethodProviders({
    JUnit4MethodProvider.class,
    JUnit3MethodProvider.class
  })
  public static class Base {
    @Test
    public void testMe() {}
  }

  @Test
  public void testSingleMethod() throws Exception {
    Result r = new JUnitCore().run(Base.class);
    Assert.assertEquals(0, r.getFailureCount());
    Assert.assertEquals(1, r.getRunCount());
  }
}
