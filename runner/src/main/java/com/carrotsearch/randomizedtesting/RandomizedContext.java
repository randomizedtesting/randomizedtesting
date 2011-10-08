package com.carrotsearch.randomizedtesting;

import java.util.ArrayDeque;
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
  final ArrayDeque<Randomness> randomnesses = new ArrayDeque<Randomness>();

  /** @see Nightly */
  private final boolean nightlyMode;

  /** Master seed/ randomness. */
  private final Randomness runnerRandomness;

  RandomizedContext(Randomness runnerRandomness, Class<?> targetClass, boolean nightlyMode) {
    this.targetClass = targetClass;
    this.nightlyMode = nightlyMode;
    this.runnerRandomness = runnerRandomness;
  }

  /** The class (suite) being tested. */
  public Class<?> getTargetClass() {
    return targetClass;
  }

  /** Master seed/ randomness. */
  Randomness getRunnerRandomness() {
    return runnerRandomness;
  }

  /**
   * Returns the runner's seed, formatted.
   */
  public String getRunnerSeed() {
    return Randomness.formatSeed(getRunnerRandomness().seed);
  }

  /** Source of randomness for the context's thread. */
  public Randomness getRandomness() {
    return randomnesses.peek();
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

  /** Push a new randomness on top of the stack. */
  void push(Randomness rnd) {
    randomnesses.push(rnd);
  }

  /** Push a new randomness on top of the stack. */
  Randomness pop() {
    return randomnesses.pop();
  }
}

