package com.carrotsearch.randomizedtesting;

import java.util.Random;

/**
 * A random with a delegate, but preventing {@link Random#setSeed(long)}.
 */
@SuppressWarnings("serial")
final class RandomNoSetSeed extends Random {
  private final Random delegate;
  private final Thread owner;
  private final StackTraceElement[] allocationStack;

  /** 
   * Track out-of-context use of this {@link Random} instance. This introduces memory
   * barriers and scheduling side-effects but there's no other way to do it in any other
   * way and sharing randoms across threads or test cases is very bad and worth tracking. 
   */
  volatile boolean valid = true;

  public RandomNoSetSeed(Thread owner, Random delegate) {
    // must be here, the only Random constructor. Has side-effects on setSeed, see below.
    super(0);
    this.delegate = delegate;
    this.owner = owner;
    this.allocationStack = Thread.currentThread().getStackTrace();
  }

  @Override
  protected int next(int bits) {
    throw new RuntimeException("Shouldn't be reachable.");
  }

  @Override
  public boolean nextBoolean() {
    checkValid();
    return delegate.nextBoolean();
  }
  
  @Override
  public void nextBytes(byte[] bytes) {
    checkValid();
    delegate.nextBytes(bytes);
  }
  
  @Override
  public double nextDouble() {
    checkValid();
    return delegate.nextDouble();
  }
  
  @Override
  public float nextFloat() {
    checkValid();
    return delegate.nextFloat();
  }
  
  @Override
  public double nextGaussian() {
    checkValid();
    return delegate.nextGaussian();
  }
  
  @Override
  public int nextInt() {
    checkValid();
    return delegate.nextInt();
  }
  
  @Override
  public int nextInt(int n) {
    checkValid();
    return delegate.nextInt(n);
  }
  
  @Override
  public long nextLong() {
    checkValid();
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
    checkValid();
    return delegate.toString();
  }
  
  @Override
  public boolean equals(Object obj) {
    checkValid();
    return delegate.equals(obj);
  }
  
  @Override
  public int hashCode() {
    checkValid();
    return delegate.hashCode();
  }

  /* */
  private final void checkValid() {
    if (!valid) {
      throw new IllegalStateException("This Random instance has been invalidated and " +
      		"is probably used out of its allowed context (test or suite).");
    }
    if (Thread.currentThread() != owner) {
      Throwable allocationEx = new NotAnException("Original allocation stack for this Random.");
      allocationEx.setStackTrace(allocationStack);
      throw new IllegalStateException("This Random was created for/by another thread (" +
          owner.toString() + ")." +
          " Random instances must not be shared (acquire per-thread). Current thread: " +
          Thread.currentThread().toString(), allocationEx);
    }
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    checkValid();
    throw new CloneNotSupportedException("Don't clone test Randoms.");
  }

  // Overriding this has side effects on the GC; let's not be paranoid. 
  /* protected void finalize() throws Throwable { super.finalize(); } */  
}
