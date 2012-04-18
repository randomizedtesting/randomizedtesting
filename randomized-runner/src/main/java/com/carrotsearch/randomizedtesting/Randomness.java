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
  private final long seed;
  private final AssertingRandom random;
  private SeedDecorator[] decorators;

  public Randomness(Thread owner, long seed, SeedDecorator... decorators) {
    this.seed = seed;
    this.decorators = decorators;
    this.random = new AssertingRandom(owner, new Random(decorate(seed, decorators)));
  }

  public Randomness(long seed, SeedDecorator...decorators) {
    this(Thread.currentThread(), seed, decorators);
  }

  /** Random instance for this randomness. */
  public Random getRandom() {
    return random;
  }

  Randomness clone(Thread newOwner) {
    return new Randomness(newOwner, seed, decorators);
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
  
  /** Starting seed, read-only for tests. */
  long getSeed() {
    return seed;
  }
  
  /**
   * Decorate a given seed.
   */
  private static long decorate(long seed, SeedDecorator[] decorators) {
    for (SeedDecorator decorator : decorators) {
      seed = decorator.decorate(seed);
    }
    return seed;
  }
}