package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

public class TestStatuses {
  @Test
  public void ok() {
  }

  @Test @Ignore
  public void ignored() {
  }

  @Test
  public void ignored_a() {
    Assume.assumeTrue(false);
  }

  @Test
  public void failure() {
    Assert.assertTrue(false);
  }

  @Test
  public void error() {
    throw new RuntimeException();
  }  
}
