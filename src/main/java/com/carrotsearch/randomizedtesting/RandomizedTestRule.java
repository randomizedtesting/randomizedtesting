package com.carrotsearch.randomizedtesting;

import java.util.Random;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * A rule for randomized tests.
 */
public final class RandomizedTestRule implements MethodRule {
  /**
   * System property with an integer defining global initialization seeds for all
   * random generators. Should guarantee test reproducibility.
   */
  public static final String SYSPROP_RANDOM_SEED = "random.seed";

  /**
   * Random seed and current random state. Per-thread.
   */
  private final static class Randomness {
    final long seed;
    final Random random;

    public Randomness(long seed) {
      this.seed = seed;
      this.random = new Random();
    }
  }

  /**
   * Global random initialization seed shared by all threads under the framework's control.
   */
  private static final long globalStartSeed;
  static {
    globalStartSeed = Long.parseLong(
        System.getProperty(SYSPROP_RANDOM_SEED, 
            Long.toString(System.currentTimeMillis()))); 
  }

  /**
   * Current seeds. Seeds are thread-bound to avoid race conditions when
   * accesses to randoms are interleaved from spawned sub-threads.
   * 
   * @see #getRandom()
   */
  private final ThreadLocal<Randomness> seeds = new ThreadLocal<Randomness>();

  /**
   * Return per-thread Random instance. This method currently fails with an exception
   * for threads not under framework's control. 
   */
  public Random getRandom() {
    final Randomness rnd = seeds.get();
    if (rnd == null)
      throw new AssertionError("Randomness not initialized for this thread: " + Thread.currentThread());
    return rnd.random;
  }

  /**
   * A wrapper around test method execution. Handles seed resets etc. 
   */
  public Statement apply(final Statement base, final FrameworkMethod method, Object target) {
    return new Statement() {
      public void evaluate() throws Throwable {
        // TODO: add detection of a "repeatable failure" with the given seed here somehow?
        // TODO: add thread timeouts/ deadlock detection?
        try {
          seeds.set(initPerThreadRandom());
          base.evaluate();
        } catch (Throwable t) {
          handleTestException(t, method);
        } finally {
          seeds.remove();
        }
      }
    };
  }

  /**
   * Handle a Throwable thrown during test execution. We do some magic here to include
   * the randomness seed information (so that the execution of this test can be repeated).
   */
  private void handleTestException(Throwable t, FrameworkMethod method) throws Throwable {
    System.err.println("Randomized test case failed: " + method.getName() + 
        ", [hopefully] repeat with: -D" + SYSPROP_RANDOM_SEED + "=" + getRandomness().seed);
    // TODO: augment t to include randomness info.
    // TODO: move syserr to an event listener.
    throw t;
  }

  /**
   * Return per-thread {@link Randomness} instance. 
   */
  public Randomness getRandomness() {
    assert seeds.get() != null : "Not initialized. Not running under a JUnit Statement?";
    return seeds.get();
  }
  
  /**
   * Returns a fresh per-thread {@link Randomness}, initialized to the same global starting seed.
   */
  private static Randomness initPerThreadRandom() {
    // Randomness can be globally "fixed" to a given starting seed.
    return new Randomness(globalStartSeed);
  }
}
