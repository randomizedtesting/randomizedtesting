package com.carrotsearch.examples.randomizedrunner.reports;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

public class Test001AllStatuses {
  @Test
  public void passed() {}
  
  @Test
  @Ignore
  public void ignored() {}

  @Test
  public void ignored_assumption() {
    Assume.assumeTrue(false);
  }

  @Test
  public void failure() {
    Assert.fail();
  }

  @Test
  public void error() {
    throw new RuntimeException();
  }
}
