package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.AfterClass;
import org.junit.Test;

public class TestAfterClassError {
  @AfterClass
  public static void afterClass() {
    throw new RuntimeException();
  }

  @Test
  public void method() {}
}
