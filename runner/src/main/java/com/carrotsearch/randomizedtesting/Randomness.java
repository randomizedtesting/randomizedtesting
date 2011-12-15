package com.carrotsearch.randomizedtesting;

import java.util.Random;

/**
 * Per-thread, per-lifecycle state randomness defined as an initial seed and 
 * the current Random instance.
 * 
 * <p>An instance of this class will be typically available from {@link RandomizedContext}.
 * No need to instantiate manually.
 * 
 * @see RandomizedContext
 */
public final class Randomness {
  final long seed;
  final Random random;

  public Randomness(long seed) {
    this.seed = seed;
    this.random = new RandomNoSetSeed(new Random(seed));
  }

  /** Random instance for this randomness. */
  public Random getRandom() {
    return random;
  }  

  /** Starting seed, read-only. */
  public long getSeed() {
    return seed;
  }

  @Override
  public String toString() {
    return "[Randomness, seed=" + SeedUtils.formatSeedChain(this) + "]";
  }
}