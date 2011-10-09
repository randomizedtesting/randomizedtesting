package com.carrotsearch.randomizedtesting;

import java.util.Random;

/**
 * A random with a delegate, but preventing {@link Random#setSeed(long)}.
 */
@SuppressWarnings("serial")
final class RandomNoSetSeed extends Random {
  private final Random delegate;

  public RandomNoSetSeed(Random delegate) {
    super(0);
    this.delegate = delegate;
  }

  @Override
  protected int next(int bits) {
    throw new RuntimeException("Shouldn't be reachable.");
  }

  @Override
  public boolean nextBoolean() {
    return delegate.nextBoolean();
  }
  
  @Override
  public void nextBytes(byte[] bytes) {
    delegate.nextBytes(bytes);
  }
  
  @Override
  public double nextDouble() {
    return delegate.nextDouble();
  }
  
  @Override
  public float nextFloat() {
    return delegate.nextFloat();
  }
  
  @Override
  public double nextGaussian() {
    return delegate.nextGaussian();
  }
  
  @Override
  public int nextInt() {
    return delegate.nextInt();
  }
  
  @Override
  public int nextInt(int n) {
    return delegate.nextInt(n);
  }
  
  @Override
  public long nextLong() {
    return delegate.nextLong();
  }
  
  @Override
  public void setSeed(long seed) {
    // This is an interesting case of observing uninitialized object from an instance method
    // (this method is called from the superclass constructor).
    if (seed == 0 && delegate == null) {
      return;
    }

    throw new RuntimeException(
        RandomizedRunner.class.getSimpleName() + 
        " prevents changing the seed of its random generators to assure repeatability" +
        " of tests. If you need a mutable instance of Random, create a new instance," +
        " preferably with the initial seed aquired from this Random instance."); 
  }

  @Override
  public String toString() {
    return delegate.toString();
  }
  
  @Override
  public boolean equals(Object obj) {
    return delegate.equals(obj);
  }
  
  @Override
  public int hashCode() {
    return delegate.hashCode();
  }
}
