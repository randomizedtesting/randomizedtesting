package com.carrotsearch.examples.randomizedrunner.reports;

import org.junit.BeforeClass;
import org.junit.Test;

/** */
public class Test007BeforeClassError {
  @BeforeClass
  public static void errorOnMe() {
    throw new RuntimeException();
  }

  @Test
  public void noop() {
  }
}
