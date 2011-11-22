package com.carrotsearch.randomizedtesting.examples;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.RandomizedContext;
import com.carrotsearch.randomizedtesting.RandomizedRunner;

@RunWith(RandomizedRunner.class)
public class TestSeedReporting {
  @Test
  public void completeFailure() {
    Assert.assertTrue(
        RandomizedContext.current().getRandom().nextBoolean());
  }
}
