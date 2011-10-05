package com.carrotsearch.randomizedtesting;

import org.junit.Rule;

/**
 * A base class declaring a {@link RandomizedTestRule} and shortcut method delegates
 * to {@link RandomizedUtils}.
 */
public abstract class RandomizedTestBase {
  @Rule
  protected final RandomizedTestRule randomized = new RandomizedTestRule();
}
