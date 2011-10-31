package com.carrotsearch.randomizedtesting;

import java.util.*;

import com.carrotsearch.randomizedtesting.annotations.Nightly;

/**
 * Context variables for an execution of a test suite (hooks and tests) running
 * under a {@link RandomizedRunner}.
 */
public final class RandomizedContext {
  /**
   * Per thread assigned resources.
   */
  private final static class PerThreadResources {
    /**
     * Generators of pseudo-randomness. This is a queue because we stack
     * them during lifecycle phases (suite/ method level). 
     */
    final ArrayDeque<Randomness> randomnesses = new ArrayDeque<Randomness>();
  }

  /** 
   * All thread groups we're currently tracking contexts for. 
   */
  final static IdentityHashMap<ThreadGroup, RandomizedContext> contexts 
    = new IdentityHashMap<ThreadGroup, RandomizedContext>();

  /** 
   * Per thread resources for each context. 
   */
  final IdentityHashMap<Thread, PerThreadResources> perThreadResources 
    = new IdentityHashMap<Thread, PerThreadResources>();

  /** A thread group that shares this context. */
  private final ThreadGroup threadGroup;
  
  /** @see #getTargetClass() */
  private final Class<?> suiteClass;

  /** @see Nightly */
  private final boolean nightlyMode;

  /** Master seed/ randomness. */
  private final Randomness runnerRandomness;

  /** The context and all of its resources are no longer usable. */
  private volatile boolean disposed;
  
  /** */
  private RandomizedContext(ThreadGroup tg, Class<?> suiteClass, Randomness runnerRandomness, boolean nightlyMode) {
    this.threadGroup = tg;
    this.suiteClass = suiteClass;
    this.nightlyMode = nightlyMode;
    this.runnerRandomness = runnerRandomness;
  }

  /** The class (suite) being tested. */
  public Class<?> getTargetClass() {
    checkDisposed();
    return suiteClass;
  }

  /** Master seed/ randomness. */
  Randomness getRunnerRandomness() {
    return runnerRandomness;
  }

  /**
   * Returns the runner's seed, formatted.
   */
  public String getRunnerSeed() {
    checkDisposed();
    return Randomness.formatSeed(getRunnerRandomness().seed);
  }

  /** Source of randomness for the context's thread. */
  public Randomness getRandomness() {
    return getPerThread().randomnesses.peek();
  }
  
  Randomness [] getRandomnesses() {
    ArrayDeque<Randomness> randomnesses = getPerThread().randomnesses;
    return randomnesses.toArray(
        new Randomness [randomnesses.size()]);
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
    checkDisposed();
    return nightlyMode;
  }

  /**
   * @return Returns the context for the calling thread or throws an
   *         {@link IllegalStateException} if the thread is out of scope.
   */
  public static RandomizedContext current() {
    return context(Thread.currentThread());
  }

  static RandomizedContext context(Thread thread) {
    final ThreadGroup currentGroup = thread.getThreadGroup();
    if (currentGroup == null) {
      throw new IllegalStateException("No context for a terminated thread: " + thread.getName());
    }

    synchronized (contexts) {
      RandomizedContext context = contexts.get(currentGroup);
      if (context == null) {
        throw new IllegalStateException("No context information for thread: " +
            thread.getName() + " (" + thread.getThreadGroup() + "). " +
            "Is this thread running under a " +
            RandomizedRunner.class + " runner? Add @RunWith(" + RandomizedRunner.class + ".class)" +
                " to your test class. ");
      }

      synchronized (context.perThreadResources) {
        if (!context.perThreadResources.containsKey(thread)) {
          PerThreadResources perThreadResources = new PerThreadResources();
          perThreadResources.randomnesses.push(
              new Randomness(context.getRunnerRandomness().seed));
          context.perThreadResources.put(thread, perThreadResources);
        }
      }

      return context;
    }
  }

  /**
   * Create a new context bound to a thread group.
   */
  static RandomizedContext create(ThreadGroup tg, Class<?> suiteClass,
      Randomness runnerRandomness, boolean nightlyMode) {
    assert Thread.currentThread().getThreadGroup() == tg;
    synchronized (contexts) {
      RandomizedContext ctx = new RandomizedContext(tg, suiteClass, runnerRandomness, nightlyMode); 
      contexts.put(tg, ctx);
      ctx.perThreadResources.put(Thread.currentThread(), new PerThreadResources());
      return ctx;
    }
  }

  /**
   * Dispose of the context.
   */
  void dispose() {
    checkDisposed();
    synchronized (contexts) {
      disposed = true;
      contexts.remove(threadGroup);
    }
  }

  /** Push a new randomness on top of the stack. */
  void push(Randomness rnd) {
    getPerThread().randomnesses.push(rnd);
  }

  /** Push a new randomness on top of the stack. */
  Randomness pop() {
    return getPerThread().randomnesses.pop();
  }

  /** Return per-thread resources associated with the current thread. */
  private PerThreadResources getPerThread() {
    checkDisposed();
    synchronized (perThreadResources) {
      return perThreadResources.get(Thread.currentThread());
    }
  }

  /**
   * Throw an exception if disposed.
   */
  private void checkDisposed() {
    if (disposed) 
      throw new IllegalStateException("Context disposed: " + 
          toString() + " for thread: " + Thread.currentThread());
  }
}

