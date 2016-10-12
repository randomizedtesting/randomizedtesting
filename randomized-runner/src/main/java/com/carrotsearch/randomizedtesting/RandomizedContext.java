package com.carrotsearch.randomizedtesting;

import java.io.Closeable;
import java.lang.Thread.State;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Callable;

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
   * Per thread resources for each context. Allow GCing of threads. 
   */
  final WeakHashMap<Thread, PerThreadResources> perThreadResources 
    = new WeakHashMap<Thread, PerThreadResources>();

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
  
  private Method currentMethod;

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
    return runner.runnerRandomness.getSeed();
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
    return getPerThread().randomnesses.peekFirst();
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
   * Return <code>true</code> if {@link Nightly} test group is explicitly enabled.
   *
   * @see RandomizedContext#getGroupEvaluator()
   * @see GroupEvaluator#isGroupEnabled(Class)
   */
  public boolean isNightly() {
    checkDisposed();
    return getGroupEvaluator().isGroupEnabled(Nightly.class);
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
   * Provide access to {@link GroupEvaluator}.
   */
  public GroupEvaluator getGroupEvaluator() {
    return runner.groupEvaluator;
  }

  /**
   * Pushes the given randomness to the top of the stack, runs the {@link Callable} and disposes
   * the randomness before the this method returns.
   * <p>
   * This utility method can be used to initialize resources in a reproducible way since all calls to utility methods
   * like {@link com.carrotsearch.randomizedtesting.RandomizedTest#randomInt()} et.al. are forwarded to the current
   * RandomContext which uses the provided randomness from the top of the stack.
   * </p>
   *
   * @param randomness the randomness to push to the top of the stack
   * @param callable the callable to execute
   * @param <T> the return type of the callable
   * @return the result of the call to {@link java.util.concurrent.Callable#call()}
   * @throws Exception if {@link java.util.concurrent.Callable#call()} throws an exception
   */
  public <T> T runWithPrivateRandomness(Randomness randomness, Callable<T> callable) throws Exception {
    push(randomness);
    try {
      return callable.call();
    } finally {
      popAndDestroy();
    }
  }

  /**
   * Pushes the given randomness to the top of the stack, runs the {@link Callable} and disposes
   * the randomness before the this method returns.
   * <p>
   * This utility method can be used to initialize resources in a reproducible way since all calls to utility methods
   * like {@link com.carrotsearch.randomizedtesting.RandomizedTest#randomInt()} et.al. are forwarded to the current
   * RandomContext which uses the provided randomness from the top of the stack.
   * </p>
   *
   * @param seed The initial seed for the new, private randomness
   * @param callable the callable to execute
   * @param <T> the return type of the callable
   * @return the result of the call to {@link java.util.concurrent.Callable#call()}
   * @throws Exception if {@link java.util.concurrent.Callable#call()} throws an exception
   */
  public <T> T runWithPrivateRandomness(long seed, Callable<T> callable) throws Exception {
    Randomness randomness = getRandomness();
    Randomness prv = new Randomness(seed, randomness.getRandomSupplier(), randomness.getDecorators());
    return runWithPrivateRandomness(prv, callable);
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
    ThreadGroup currentGroup = thread.getThreadGroup();
    if (currentGroup == null) {
      throw new IllegalStateException("No context for a terminated thread: " + Threads.threadName(thread));
    }

    synchronized (_globalLock) {
      RandomizedContext context;
      while (true) {
        context = contexts.get(currentGroup);
        if (context == null && currentGroup.getParent() != null) {
          currentGroup = currentGroup.getParent();
        } else {
          break;
        }
      }

      if (context == null) {
        throw new IllegalStateException("No context information for thread: " +
            Threads.threadName(thread) + ". " +
            "Is this thread running under a " +
            RandomizedRunner.class + " runner context? Add @RunWith(" + RandomizedRunner.class + ".class)" +
                " to your test class. Make sure your code accesses random contexts within "
                + "@BeforeClass and @AfterClass boundary (for example, static test class initializers are "
                + "not permitted to access random contexts).");
      }

      synchronized (context._contextLock) {
        if (!context.perThreadResources.containsKey(thread)) {
          PerThreadResources perThreadResources = new PerThreadResources();
          perThreadResources.randomnesses.push(
              context.runner.runnerRandomness.clone(thread));
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
        for (PerThreadResources ref : perThreadResources.values()) {
          if (ref != null) {
            for (Randomness randomness : ref.randomnesses) {
              randomness.destroy();
            }
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
   * Clone context information between the current thread and another thread.
   * This is for internal use only to propagate context information when forking.
   */
  static void cloneFor(Thread t) {
    if (t.getState() != State.NEW) {
      throw new IllegalStateException("The thread to share context with is not in NEW state: " + t);
    }
    
    final ThreadGroup tGroup = t.getThreadGroup();
    if (tGroup == null) {
      throw new IllegalStateException("No thread group for thread: " + t);
    }

    Thread me = Thread.currentThread();
    if (me.getThreadGroup() != tGroup) {
      throw new IllegalArgumentException("Both threads must share the thread group.");
    }

    synchronized (_globalLock) {
      RandomizedContext context = contexts.get(tGroup);
      if (context == null) {
        throw new IllegalStateException("No context information for thread: " + t);
      }

      synchronized (context._contextLock) {
        if (context.perThreadResources.containsKey(t)) {
          throw new IllegalStateException("Context already initialized for thread: " + t);
        }
        
        if (!context.perThreadResources.containsKey(me)) {
          throw new IllegalStateException("Context not initialized for thread: " + me);
        }

        PerThreadResources perThreadResources = new PerThreadResources();
        for (Randomness r : context.perThreadResources.get(me).randomnesses) {
          perThreadResources.randomnesses.addLast(r.clone(t));          
        }
        context.perThreadResources.put(t, perThreadResources);
      }
    }
  }

  void setTargetMethod(Method method) {
    this.currentMethod = method;
  }
  
  /**
   * @return Return the currently executing test case method (the thread may still 
   * be within test rules and may never actually hit the method). This method may return
   * <code>null</code> if called from the static context (no test case is being executed at
   * the moment).
   */
  public Method getTargetMethod() {
    checkDisposed();
    return currentMethod;
  }
}
