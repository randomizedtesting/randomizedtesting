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
  private final AssertingRandom random;

  public Randomness(Thread owner, long seed) {
    this.seed = seed;
    this.random = new AssertingRandom(owner, new Random(seed));
  }

  public Randomness(long seed) {
    this(Thread.currentThread(), seed);
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

  /**
   * Invalidate the underling {@link #random}.
   * 
   * @see AssertingRandom#valid
   */
  void destroy() {
    this.random.destroy();
  }
}