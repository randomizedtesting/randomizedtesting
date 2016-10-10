package com.carrotsearch.ant.tasks.junit4.spikes;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.annotations.Nightly;
import com.carrotsearch.randomizedtesting.annotations.TestCaseOrdering;

@Ignore
@RunWith(RandomizedRunner.class)
@TestCaseOrdering(TestCaseOrdering.AlphabeticOrder.class)
public class TestStatusesIDE {
  @Test
  public void _001_ok() {
  }

  @Test @Ignore
  public void _002_ignored() {
  }

  @Test
  public void _003_ignoredByExplicitAssumption() {
    Assume.assumeTrue(false);
  }

  @Test @Nightly
  public void _004_ignoredByNightlyGroup() {
  }

  @Test
  public void _005_failedWithAssertion() {
    Assert.assertTrue(false);
  }

  @Test
  public void _006_failedWithException() {
    throw new RuntimeException();
  }
}
