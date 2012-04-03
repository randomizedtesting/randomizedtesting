package com.carrotsearch.ant.tasks.junit4.tests.sub2;

import org.junit.Test;

public class TestHalfSecond {
  @Test
  public void halfSecond() throws Exception {
    Thread.sleep(500);
  }
}
