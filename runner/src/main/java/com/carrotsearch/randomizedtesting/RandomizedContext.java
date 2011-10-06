package com.carrotsearch.randomizedtesting;

/**
 * Context variables for an execution of a test suite (hooks and tests) running
 * under a {@link RandomizedRunner}.
 */
public final class RandomizedContext {
  /** Per thread context. */
  private final static ThreadLocal<RandomizedContext> context = new ThreadLocal<RandomizedContext>();

  /** @see #getTargetClass() */
  Class<?> targetClass;

  /** @see #getRandomness() */
  Randomness randomness;

  /** The class (suite) being tested. */
  public Class<?> getTargetClass() {
    return targetClass;
  }

  /** Source of randomness for the context's thread. */
  public Randomness getRandomness() {
    return randomness;
  }

  /**
   * Sets the context for the current thread.
   */
  static void setContext(RandomizedContext ctx) {
    context.set(ctx);
  }

  /**
   * Clear the context for the current thread.
   */
  static void clearContext() {
    setContext(null);
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

