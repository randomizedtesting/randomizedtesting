package com.carrotsearch.ant.tasks.junit4.tests;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class HeartbeatSlow {
  @Test
  public void method1() throws Exception {
    Thread.sleep(TimeUnit.SECONDS.toMillis(3));
  }
}
