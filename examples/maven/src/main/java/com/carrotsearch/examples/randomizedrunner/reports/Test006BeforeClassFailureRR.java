package com.carrotsearch.examples.randomizedrunner.reports;

import org.junit.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.RandomizedRunner;

/** */
@RunWith(RandomizedRunner.class)
public class Test006BeforeClassFailureRR {
  @BeforeClass
  public static void failOnMe() {
    Assert.assertTrue(false);
  }

  @Test
  public void noop() {
  }
}
