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
  private final Random random;

  private final RandomSupplier supplier;
  private final SeedDecorator[] decorators;

  public Randomness(Thread owner, RandomSupplier supplier, long seed, SeedDecorator... decorators) {
    this.seed = seed;
    this.decorators = decorators;
    this.supplier = supplier;

    Random delegate = supplier.get(decorate(seed, decorators));
    if (AssertingRandom.isVerifying()) {
      this.random = new AssertingRandom(owner, delegate);
    } else {
      this.random = delegate;
    }
  }

  public Randomness(long seed, RandomSupplier supplier, SeedDecorator...decorators) {
    this(Thread.currentThread(), supplier, seed, decorators);
  }

  /** Random instance for this randomness. */
  public Random getRandom() {
    return random;
  }
  
  RandomSupplier getRandomSupplier() {
    return supplier;
  }

  SeedDecorator[] getDecorators() {
    return decorators;
  }

  Randomness clone(Thread newOwner) {
    return new Randomness(newOwner, supplier, seed, decorators);
  }

  @Override
  public String toString() {
    return "[Randomness, seed=" + SeedUtils.formatSeedChain(this) + "]";
  }

  /**
   * Invalidate the underling randomness.
   */
  void destroy() {
    if (random instanceof AssertingRandom) {
      ((AssertingRandom) random).destroy();
    }
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