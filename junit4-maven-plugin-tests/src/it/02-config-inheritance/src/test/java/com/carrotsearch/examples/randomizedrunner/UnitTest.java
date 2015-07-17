package com.carrotsearch.examples.randomizedrunner;

import org.junit.Assert;
import org.junit.Test;

public class UnitTest {
  @Test
  public void commonArg() {
      Assert.assertEquals("arg.common", System.getProperty("arg.common"));
  }

  @Test
  public void unitConfigArg() {
      Assert.assertEquals("arg.unit", System.getProperty("arg.unit"));
  }
}
