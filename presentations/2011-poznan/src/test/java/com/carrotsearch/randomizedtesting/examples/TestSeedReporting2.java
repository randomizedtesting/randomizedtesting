package com.carrotsearch.randomizedtesting.examples;

import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.*;

public class TestSeedReporting2 extends RandomizedTest {
  @Test
  public void completeFailure() {
    Assert.assertTrue(randomBoolean());
  }

  @Seed("deadbeef")
  @Repeat(iterations = 10, useConstantSeed = true)
  @Test
  public void completeFailure2() {
    Assert.assertTrue(randomBoolean());
  }
  
  @Seed("deadbeef")
  @Repeat(iterations = 10)
  @Test
  public void completeFailure3() {
    Assert.assertTrue(randomBoolean());
  }  
  
  @Seeds({
    @Seed("deadbeef"),
    @Seed("cafebabe"),
    @Seed("deadbaba"),
    @Seed("baadf00d"),
    @Seed()
  })
  @Test
  public void completeFailure4() {
    Assert.assertTrue(randomBoolean());
  }  
}
