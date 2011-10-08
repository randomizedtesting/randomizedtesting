package com.carrotsearch.randomizedtesting;

import java.util.Random;

import com.carrotsearch.randomizedtesting.annotations.Nightly;

/**
 * Context variables for an execution of a test suite (hooks and tests) running
 * under a {@link RandomizedRunner}.
 */
public final class RandomizedContext {
  /** Per thread context. */
  private final static ThreadLocal<RandomizedContext> context = new ThreadLocal<RandomizedContext>();

  /** @see #getTargetClass() */
  final Class<?> targetClass;

  /** @see #getRandomness() */
  Randomness randomness;
  
  /** @see Nightly */
  private final boolean nightlyMode;

  RandomizedContext(Class<?> targetClass, boolean nightlyMode) {
    this.targetClass = targetClass;
    this.nightlyMode = nightlyMode;
  }

  /** The class (suite) being tested. */
  public Class<?> getTargetClass() {
    return targetClass;
  }

  /** Source of randomness for the context's thread. */
  public Randomness getRandomness() {
    return randomness;
  }

  /**
   * A shorthand for calling {@link #getRandomness()} and then {@link Randomness#getRandom()}. 
   */
  public Random getRandom() {
    return getRandomness().getRandom();
  }

  /**
   * Return <code>true</code> if tests are running in nightly mode.
   */
  public boolean isNightly() {
    return nightlyMode;
  }

  /**
   * Sets the context for the current thread.
   */
  static void setContext(RandomizedContext ctx) {
    if (context.get() != null) {
      throw new Error("Recursive context stack not implemented (yet).");
    }
    context.set(ctx);
  }

  /**
   * Clear the context for the current thread.
   */
  static void clearContext() {
    context.set(null);
  }

  /**
   * @return Returns the context for the calling thread or throws an
   *         {@link IllegalStateException} if the thread is out of scope.
   */
  public static RandomizedContext current() {
    RandomizedContext ctx = context.get();
    if (ctx == null) {
      throw new IllegalStateException("No context information, is this test/ thread running under " +
      		RandomizedRunner.class + " runner? Add @RunWith(" + RandomizedRunner.class + ".class)" +
      				" to your test class");
    }
    return ctx;
  }
}

