package com.carrotsearch.ant.tasks.junit4.tests;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.*;
import static com.carrotsearch.randomizedtesting.SysGlobals.*;

public class TestSeedPassing extends RandomizedTest {
  @Test
  public void checkSeedSet() throws IOException {
    Assert.assertEquals(System.getProperty(SYSPROP_RANDOM_SEED()),
        SeedUtils.formatSeedChain(
            new Randomness(0xdeadbeefL),
            new Randomness(0xcafebabeL)), 
            System.getProperty(SYSPROP_RANDOM_SEED()));

    Assert.assertEquals(
        "[CAFEBABE]",
        SeedUtils.formatSeedChain(getContext().getRandomness()));

    Assert.assertEquals(
        "[DEADBEEF]",
        SeedUtils.formatSeedChain(
            new Randomness(
                SeedUtils.parseSeed(getContext().getRunnerSeedAsString()))));
  }
}
