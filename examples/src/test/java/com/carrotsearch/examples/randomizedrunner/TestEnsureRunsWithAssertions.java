package com.carrotsearch.examples.randomizedrunner;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Validators;
import com.carrotsearch.randomizedtesting.validators.EnsureAssertionsEnabled;

/**
 * This test suite will fail immediately if running without global -ea.
 */
@Validators(value = {EnsureAssertionsEnabled.class})
public class TestEnsureRunsWithAssertions extends RandomizedTest {
  @Test
  public void dummy() {
    // Do nothing.
  }
}
