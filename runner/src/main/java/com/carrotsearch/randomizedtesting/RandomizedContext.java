package com.carrotsearch.randomizedtesting;

import java.io.Closeable;
import java.lang.annotation.Annotation;
import java.util.*;

import com.carrotsearch.randomizedtesting.annotations.Nightly;

/**
 * Context variables for an execution of a test suite (hooks and tests) running
 * under a {@link RandomizedRunner}.
 */
public final class RandomizedContext {
  /** Coordination at global level. */
  private static final Object _globalLock = new Object();
  /** Coordination at context level. */
  private        final Object _contextLock = new Object();

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

  /** The runner to which we're bound. */
  private final RandomizedRunner runner;

  /** The context and all of its resources are no longer usable. */
  private volatile boolean disposed;

  /**
   * Disposable resources.
   */
  private EnumMap<LifecycleScope, List<CloseableResourceInfo>> disposableResources
    = new EnumMap<LifecycleScope, List<CloseableResourceInfo>>(LifecycleScope.class);

  /** */
  private RandomizedContext(ThreadGroup tg, Class<?> suiteClass, RandomizedRunner runner) {
    this.threadGroup = tg;
    this.suiteClass = suiteClass;
    this.runner = runner;
  }

  /** The class (suite) being tested. */
  public Class<?> getTargetClass() {
    checkDisposed();
    return suiteClass;
  }

  /** Runner's seed. */
  long getRunnerSeed() {
    return runner.runnerRandomness.seed;
  }

  /**
   * Returns the runner's master seed, formatted.
   */
  public String getRunnerSeedAsString() {
    checkDisposed();
    return SeedUtils.formatSeed(getRunnerSeed());
  }

  /** Source of randomness for the context's thread. */
  public Randomness getRandomness() {
    return getPerThread().randomnesses.peek();
  }

  /**
   * Return all {@link Randomness} on the stack for the current thread. The most
   * recent (currently used) randomness comes last in this array.
   */
  Randomness [] getRandomnesses() {
    ArrayDeque<Randomness> randomnesses = getPerThread().randomnesses;
    Randomness[] array = randomnesses.toArray(new Randomness [randomnesses.size()]);
    for (int i = 0, j = array.length - 1; i < j; i++, j--) {
        Randomness r = array[i];
        array[i] = array[j];
        array[j] = r;
    }
    return array;
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
    return getTestGroups().get(Nightly.class).isEnabled();
  }

  /**
   * @return Returns the context for the calling thread or throws an
   *         {@link IllegalStateException} if the thread is out of scope.
   * @throws IllegalStateException If context is not available.
   */
  public static RandomizedContext current() {
    return context(Thread.currentThread());
  }

  /**
   * Access to the runner governing this context.
   */
  public RandomizedRunner getRunner() {
    return runner;
  }
  
  /**
   * Dispose the given resource at the end of a given lifecycle scope. If the {@link Closeable}
   * throws an exception, the test case or suite will end in a failure.
   * 
   * @return <code>resource</code> (for call chaining).
   */
  public <T extends Closeable> T closeAtEnd(T resource, LifecycleScope scope) {
    synchronized (_contextLock) {
      List<CloseableResourceInfo> resources = disposableResources.get(scope);
      if (resources == null) {
        disposableResources.put(scope, resources = new ArrayList<CloseableResourceInfo>());
      }
      resources.add(new CloseableResourceInfo(
          resource, scope, Thread.currentThread(), Thread.currentThread().getStackTrace()));
      return resource;
    }
  }

  /**
   * Dispose of any resources registered in the given scope.
   */
  void closeResources(ObjectProcedure<CloseableResourceInfo> consumer, LifecycleScope scope) {
    List<CloseableResourceInfo> resources;
    synchronized (_contextLock) {
      resources = disposableResources.remove(scope);
    }

    if (resources != null) {
      for (CloseableResourceInfo info : resources) {
        consumer.apply(info);
      }
    }
  }

  static RandomizedContext context(Thread thread) {
    final ThreadGroup currentGroup = thread.getThreadGroup();
    if (currentGroup == null) {
      throw new IllegalStateException("No context for a terminated thread: " + thread.getName());
    }

    synchronized (_globalLock) {
      RandomizedContext context = contexts.get(currentGroup);
      if (context == null) {
        throw new IllegalStateException("No context information for thread: " +
            thread.getName() + " (" + thread.getThreadGroup() + "). " +
            "Is this thread running under a " +
            RandomizedRunner.class + " runner? Add @RunWith(" + RandomizedRunner.class + ".class)" +
                " to your test class. ");
      }

      synchronized (context._contextLock) {
        if (!context.perThreadResources.containsKey(thread)) {
          PerThreadResources perThreadResources = new PerThreadResources();
          perThreadResources.randomnesses.push(
              new Randomness(thread, context.getRunnerSeed()));
          context.perThreadResources.put(thread, perThreadResources);
        }
      }

      return context;
    }
  }

  /**
   * Create a new context bound to a thread group.
   */
  static RandomizedContext create(ThreadGroup tg, Class<?> suiteClass, RandomizedRunner runner) {
    assert Thread.currentThread().getThreadGroup() == tg;
    synchronized (_globalLock) {
      RandomizedContext ctx = new RandomizedContext(tg, suiteClass, runner); 
      contexts.put(tg, ctx);
      ctx.perThreadResources.put(Thread.currentThread(), new PerThreadResources());
      return ctx;
    }
  }

  /**
   * Dispose of the context.
   */
  void dispose() {
    synchronized (_globalLock) {
      checkDisposed();
      disposed = true;
      contexts.remove(threadGroup);

      // Clean up and invalidate any per-thread published randoms.
      synchronized (_contextLock) {
        for (PerThreadResources ptr : perThreadResources.values()) {
          for (Randomness randomness : ptr.randomnesses) {
            randomness.destroy();
          }
        }
      }
    }
  }

  /** Push a new randomness on top of the stack. */
  void push(Randomness rnd) {
    getPerThread().randomnesses.push(rnd);
  }

  /** Pop a randomness off the stack and dispose it. */
  void popAndDestroy() {
    getPerThread().randomnesses.pop().destroy();
  }

  /** Return per-thread resources associated with the current thread. */
  private PerThreadResources getPerThread() {
    checkDisposed();
    synchronized (_contextLock) {
      return perThreadResources.get(Thread.currentThread());
    }
  }

  /**
   * Throw an exception if disposed.
   */
  private void checkDisposed() {
    if (disposed) {
      throw new IllegalStateException("Context disposed: " + 
          toString() + " for thread: " + Thread.currentThread());
    }
  }

  /**
   * Provide access to test groups.
   */
  HashMap<Class<? extends Annotation>,RuntimeTestGroup> getTestGroups() {
    return runner.testGroups;
  }
}

