package com.carrotsearch.randomizedtesting;

import java.util.Random;

/**
 * Random seed and current random state. Per-thread.
 */
public final class Randomness {
  final long seed;
  final Random random;

  public Randomness(long seed) {
    this.seed = seed;
    this.random = new Random();
  }
}