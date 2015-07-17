package com.carrotsearch.examples.randomizedrunner;

import org.junit.Assert;
import org.junit.Test;

public class IntegrationIT {
  @Test
  public void commonArg() {
      Assert.assertEquals("arg.common", System.getProperty("arg.common"));
  }

  @Test
  public void itConfigArg() {
      Assert.assertEquals("arg.it", System.getProperty("arg.it"));
  }
}
