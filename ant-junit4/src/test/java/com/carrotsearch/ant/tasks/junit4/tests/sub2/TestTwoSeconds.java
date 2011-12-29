package com.carrotsearch.ant.tasks.junit4.tests.sub2;

import org.junit.Test;

public class TestTwoSeconds {
  @Test
  public void oneSecond1() throws Exception {
    Thread.sleep(1000);
  }

  @Test
  public void oneSecond2() throws Exception {
    Thread.sleep(1000);
  }  
}
