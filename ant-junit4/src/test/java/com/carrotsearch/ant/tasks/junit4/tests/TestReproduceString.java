package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Listeners;
import com.carrotsearch.randomizedtesting.listeners.ReproduceInfoPrinter;

@Listeners({
  ReproduceInfoPrinter.class
})
public class TestReproduceString extends RandomizedTest {
  @Test
  public void alwaysFail() {
    Assert.fail();
  }
}
