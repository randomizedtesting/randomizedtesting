package com.carrotsearch.ant.tasks.junit4.tests;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.*;

public class TestSeedPassing {
  @Test
  public void checkSeedSet() throws IOException {
    Assert.assertEquals(System.getProperty(RandomizedRunner.SYSPROP_RANDOM_SEED),
        SeedUtils.formatSeedChain(
            new Randomness(0xdeadbeefL),
            new Randomness(0xcafebabeL)), 
            System.getProperty(RandomizedRunner.SYSPROP_RANDOM_SEED));
  }
}
