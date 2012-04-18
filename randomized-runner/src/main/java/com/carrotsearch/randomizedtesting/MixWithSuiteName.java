package com.carrotsearch.randomizedtesting;

import com.carrotsearch.randomizedtesting.annotations.SeedDecorators;

/**
 * A {@link SeedDecorator} to be used with {@link SeedDecorators} annotation
 * to modify the master {@link Randomness} with a hash off the suite's class name.
 */
public class MixWithSuiteName implements SeedDecorator {
  private long xorHash;

  @Override
  public void initialize(Class<?> suiteClass) {
    this.xorHash = fmix64(suiteClass.getName().hashCode());
  }

  @Override
  public long decorate(long seed) {
    return seed ^ xorHash;
  }

  /** final mix from murmur hash 3. */
  private long fmix64(long k) {
    k ^= k >>> 33;
    k *= 0xff51afd7ed558ccdL;
    k ^= k >>> 33;
    k *= 0xc4ceb9fe1a85ec53L;
    k ^= k >>> 33;
    return k;
  }
}