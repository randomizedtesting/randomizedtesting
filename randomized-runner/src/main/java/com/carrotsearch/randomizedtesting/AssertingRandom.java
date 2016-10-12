package com.carrotsearch.randomizedtesting;

import java.lang.ref.WeakReference;
import java.util.Random;

/**
 * A random with a delegate, preventing {@link Random#setSeed(long)} and locked
 * to be used by a single thread.
 */
@SuppressWarnings("serial")
public final class AssertingRandom extends Random {
  private final Random delegate;
  private final WeakReference<Thread> ownerRef;
  private final String ownerName;
  private final StackTraceElement[] allocationStack;

  /** 
   * Track out-of-context use of this {@link Random} instance. This introduces memory
   * barriers and scheduling side-effects but there's no other way to do it in any other
   * way and sharing randoms across threads or test cases is very bad and worth tracking. 
   */
  private volatile boolean valid = true;

  /**
   * Enable paranoid mode when assertions are enabled.
   */
  private final static boolean assertionsEnabled = AssertingRandom.class.desiredAssertionStatus(); 

  /**
   * Creates an instance to be used by <code>owner</code> thread and delegating
   * to <code>delegate</code> until {@link #destroy()}ed.
   */
  public AssertingRandom(Thread owner, Random delegate) {
    // Must be here, the only Random constructor. Has side-effects on setSeed, see below.
    super(0);

    this.delegate = delegate;
    this.ownerRef = new WeakReference<Thread>(owner);
    this.ownerName = owner.toString();
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

    throw noSetSeed();
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

  /**
   * This object will no longer be usable after this method is called.
   */
  public void destroy() {
    this.valid = false;
  }
  
  /* */
  private final void checkValid() {
    // Fastpath if assertions are disabled.
    if (!isVerifying()) {
      return;
    }
    
    if (!valid) {
      throw new IllegalStateException("This Random instance has been invalidated and " +
      		"is probably used out of its allowed context (test or suite).");
    }

    final Thread owner = ownerRef.get();
    if (owner == null || Thread.currentThread() != owner) {
      Throwable allocationEx = new StackTraceHolder("Original allocation stack for this Random (" +
          "allocated by " + ownerName + ")");
      allocationEx.setStackTrace(allocationStack);
      throw new IllegalStateException(
          "This Random was created for/by another thread (" + ownerName + ")." +
          " Random instances must not be shared (acquire per-thread). Current thread: " + 
          Thread.currentThread().toString(), allocationEx);
    }
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    checkValid();
    throw new CloneNotSupportedException("Don't clone test Randoms.");
  }

  /**
   * @return Return <code>true</code> if this class is verifying sharing and lifecycle assertions.
   * @see "https://github.com/randomizedtesting/randomizedtesting/issues/234"
   */
  public static boolean isVerifying() {
    return assertionsEnabled;
  }

  static RuntimeException noSetSeed() {
    return new RuntimeException(
        RandomizedRunner.class.getSimpleName() + 
        " prevents changing the seed of its random generators to assure repeatability" +
        " of tests. If you need a mutable instance of Random, create a new (local) instance," +
        " preferably with the initial seed aquired from this Random instance."); 
  }

  // Overriding this has side effects on the GC; let's not be paranoid. 
  /* protected void finalize() throws Throwable { super.finalize(); } */  
}
