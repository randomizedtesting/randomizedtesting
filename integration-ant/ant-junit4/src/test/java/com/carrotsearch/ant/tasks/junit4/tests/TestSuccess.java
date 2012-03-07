package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.SeedUtils;

public class TestSuccess extends RandomizedTest {
  @Test
  public void alwaysPasses() {
    System.out.println("Seed: " + 
        SeedUtils.formatSeedChain(getContext().getRandomness()));
  }
}
