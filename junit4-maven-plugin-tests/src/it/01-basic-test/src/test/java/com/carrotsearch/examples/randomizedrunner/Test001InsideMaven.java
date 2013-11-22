package com.carrotsearch.examples.randomizedrunner;

import org.junit.Assert;
import org.junit.Test;

public class Test001InsideMaven {
  @Test
  public void success() {
    // Empty.
  }

  @Test
  public void checkArgLinePassed() {
      Assert.assertEquals("foobar", System.getProperty("argLine.property"));
      Assert.assertEquals("foobar", System.getProperty("argLine.property2"));
      Assert.assertEquals("foobar", System.getProperty("verbatim.section"));
  }
}
