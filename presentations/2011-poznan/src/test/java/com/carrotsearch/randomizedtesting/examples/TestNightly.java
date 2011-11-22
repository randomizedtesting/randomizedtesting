package com.carrotsearch.randomizedtesting.examples;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Nightly;

public class TestNightly extends RandomizedTest {
  @Test @Nightly
  public void verySlowOne() {
    // very slot test executed
    // on a CI server.
  }
}
  