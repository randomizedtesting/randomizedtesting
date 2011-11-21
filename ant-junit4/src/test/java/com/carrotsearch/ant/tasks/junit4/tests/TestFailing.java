package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Assert;
import org.junit.Test;

public class TestFailing { 
  @Test
  public void testWithError() {
    throw new RuntimeException();
  }

  @Test
  public void testWithAssertionError() {
    Assert.assertTrue(false);
  }
}
