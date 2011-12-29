package com.carrotsearch.ant.tasks.junit4.tests.sub2;

import org.junit.Test;

public class TestOneSecond {
  @Test
  public void quarterSecond1() throws Exception {
    Thread.sleep(250);
  }

  @Test
  public void quarterSecond2() throws Exception {
    Thread.sleep(250);
  }
  
  @Test
  public void quarterSecond3() throws Exception {
    Thread.sleep(250);
  }  

  @Test
  public void quarterSecond4() throws Exception {
    Thread.sleep(250);
  }  
}
