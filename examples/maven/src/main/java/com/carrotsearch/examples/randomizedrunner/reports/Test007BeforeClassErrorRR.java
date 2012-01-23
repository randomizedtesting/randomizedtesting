package com.carrotsearch.examples.randomizedrunner.reports;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.RandomizedRunner;

/** */
@RunWith(RandomizedRunner.class)
public class Test007BeforeClassErrorRR {
  @BeforeClass
  public static void errorOnMe() {
    throw new RuntimeException();
  }

  @Test
  public void noop() {
  }
}
