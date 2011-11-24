package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

public class TestIgnored {
  @Test @Ignore
  public void method1() {
  }

  @Test
  public void method2() {
    Assume.assumeTrue(false);
  }
}
