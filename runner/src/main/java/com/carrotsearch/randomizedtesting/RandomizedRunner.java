package com.carrotsearch.randomizedtesting;


import static com.carrotsearch.randomizedtesting.MethodCollector.allDeclaredMethods;
import static com.carrotsearch.randomizedtesting.MethodCollector.annotatedWith;
import static com.carrotsearch.randomizedtesting.MethodCollector.flatten;
import static com.carrotsearch.randomizedtesting.MethodCollector.immutableCopy;
import static com.carrotsearch.randomizedtesting.MethodCollector.mutableCopy;
import static com.carrotsearch.randomizedtesting.MethodCollector.removeOverrides;
import static com.carrotsearch.randomizedtesting.MethodCollector.removeShadowed;
import static com.carrotsearch.randomizedtesting.MethodCollector.sort;
import static com.carrotsearch.randomizedtesting.Randomness.formatSeedChain;
import static com.carrotsearch.randomizedtesting.Randomness.parseSeedChain;

import java.lang.Thread.State;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import com.carrotsearch.randomizedtesting.annotations.*;

/**
 * A somewhat less hairy (?), no-fancy {@link Runner} implementation for 
 * running randomized test cases with predictable and repeatable randomness.
 * 
 * <p>Supports the following JUnit4 features:
 * <ul>
 *   <li>{@link BeforeClass}-annotated methods (before all tests of a class/superclass),</li>
 *   <li>{@link Before}-annotated methods (before each test),</li>
 *   <li>{@link Test}-annotated methods,</li>
 *   <li>{@link After}-annotated methods (after each test),</li>
 *   <li>{@link AfterClass}-annotated methods (after all tests of a class/superclass),</li>
 *   <li>{@link Rule}-annotated fields implementing <code>MethodRule</code>.</li>
 * </ul>
 * 
 * <p>Contracts:
 * <ul>
 *   <li>{@link BeforeClass}, {@link Before}
 *   methods declared in superclasses are called before methods declared in subclasses,</li>
 *   <li>{@link AfterClass}, {@link After}
 *   methods declared in superclasses are called after methods declared in subclasses,</li>
 *   <li>{@link BeforeClass}, {@link Before}, {@link AfterClass}, {@link After}
 *   methods declared within the same class are called in <b>randomized</b> order
 *   derived from the master seed (repeatable with the same seed),</li>
 *   <li>
 * </ul>
 * 
 * <p>Deviations from "standard" JUnit:
 * <ul>
 *   <li>test methods are allowed to return values (the return value is ignored),</li>
 *   <li>hook methods need not be public; in fact, it is encouraged to make them private to
 *       avoid accidental shadowing which silently drops parent hooks from executing
 *       (applies to class hooks mostly, but also to instance hooks).</li> 
 *   <li>all exceptions raised during hooks or test case execution are reported to the notifier,
 *       there is no suppression or chaining of exceptions,</li>
 * </ul>
 * 
 * @see RandomizedTest
 * @see Validators
 * @see Listeners
 * @see RandomizedContext
 */
public final class RandomizedRunner extends Runner implements Filterable {
  /**
   * System property with an integer defining global initialization seeds for all
   * random generators. Should guarantee test reproducibility.
   */
  public static final String SYSPROP_RANDOM_SEED = "tests.seed";

  /**
   * Global system property indicating that we're running nightly tests.
   * 
   * @see Nightly
   */
  public static final String SYSPROP_NIGHTLY = "tests.nightly";

  /**
   * The global override for the number of each test's repetitions.
   */
  public static final String SYSPROP_ITERATIONS = "tests.iters";

  /**
   * Global override for picking out a single test class to execute. All other
   * classes are ignored. 
   */
  public static final String SYSPROP_TESTCLASS = "tests.class";

  /**
   * Global override for picking out a single test method to execute. If a
   * matching method exists in more than one class, it will be executed. 
   */
  public static final String SYSPROP_TESTMETHOD = "tests.method";

  /**
   * If there's a runaway thread, how many times do we try to interrupt and
   * then kill it before we give up? Runaway threads may affect other tests (bad idea).
   *  
   * @see #SYSPROP_KILLWAIT
   */
  public static final String SYSPROP_KILLATTEMPTS = "tests.killattempts";

  /**
   * If there's a runaway thread, how long should we wait between iterations of 
   * putting a silver bullet through its heart?
   * 
   * @see #SYSPROP_KILLATTEMPTS
   */
  public static final String SYSPROP_KILLWAIT = "tests.killwait";

  /**
   * Global override for a single test case's maximum execution time after which
   * it is considered out of control and an attempt to interrupt it is executed.
   * Timeout in millis. 
   */
  public static final String SYSPROP_TIMEOUT = "tests.timeout";

  /**
   * Fake package of a stack trace entry inserted into exceptions thrown by 
   * test methods. These stack entries contain additional information about
   * seeds used during execution. 
   */
  public static final String AUGMENTED_SEED_PACKAGE = "__randomizedtesting";
  
  /**
   * Default timeout for a single test case: 60 seconds. Use global system property
   * {@link #SYSPROP_TIMEOUT} or an annotation {@link Timeout} if you need more. 
   * Annotation takes precedence, if defined. 
   */
  public static final int DEFAULT_TIMEOUT = 1000 * 60;

  /**
   * The default number of first interrupts, then Thread.stop attempts.
   */
  public static final int DEFAULT_KILLATTEMPTS = 10;
  
  /**
   * Time in between interrupt retries or stop retries.
   */
  public static final int DEFAULT_KILLWAIT = 1000;

  /**
   * The default number of iterations.
   */
  public static final int DEFAULT_ITERATIONS = 1;

  /**
   * Test candidate (model).
   */
  private static class TestCandidate {
    public final Randomness randomness;
    public final Description description;
    public final Method method;

    public TestCandidate(Method method, Randomness rnd, Description description) {
      this.randomness = rnd;
      this.description = description;
      this.method = method;
    }
  }

  /**
   * 
   */
  private final static Logger logger = Logger.getLogger(RandomizedRunner.class.getSimpleName());

  /** 
   * A sequencer for affecting the initial seed in case of rapid succession of this class
   * instance creations. Not likely, but can happen two could get the same seed.
   */
  private final static AtomicLong sequencer = new AtomicLong();
  
  /** The class with test methods (suite). */
  private final Class<?> suiteClass;

  /** 
   * All methods of the {@link #suiteClass} class, unfiltered (including overrides and shadowed
   * methods). Sorted at class level to the order: class..super, and at method level (within
   * each class) alphabetically.
   */
  private List<List<Method>> allTargetMethods;

  /** The runner's seed (master). */
  private final Randomness runnerRandomness;

  /** 
   * If {@link #SYSPROP_RANDOM_SEED} property is used with two arguments (master:method)
   * then this field contains method-level override. 
   */
  private Randomness testCaseRandomnessOverride;

  /** 
   * The number of each test's randomized iterations.
   * 
   * @see #SYSPROP_ITERATIONS
   */
  private final Integer iterationsOverride;

  /**
   * Test case timeout in millis.
   * 
   * @see #SYSPROP_TIMEOUT
   */
  private final int timeoutOverride;

  /** All test candidates, processed (seeds assigned) and flattened. */
  private List<TestCandidate> testCandidates;

  /** Class suite description. */
  private Description suiteDescription;

  /** Applies a user-level test filter if not null. */
  private Filter filter;

  /** 
   * How many attempts to interrupt and then kill a runaway thread before giving up?
   */
  private final int killAttempts;
  
  /**
   * How long to wait between attempts to kill a runaway thread (millis). 
   */
  private final int killWait;

  /**
   * A set of threads which we could not terminate or kill no matter how hard we tried. Even by
   * driving sharpened silver pencils through their binary cyberhearts.
   */
  private final Set<Thread> bulletProofZombies = new HashSet<Thread>();

  /**
   * All tests are executed under a specified thread group so that we can have some control
   * over how many threads have been started/ stopped. System daemons shouldn't be under
   * this group.
   */
  private RunnerThreadGroup runnerThreadGroup;

  /** 
   * @see #subscribeListeners(RunNotifier) 
   */
  private final List<RunListener> autoListeners = new ArrayList<RunListener>();

  /**
   * We simply report to syserr. There should be no threads out of runner's control.
   * This can also be validated with aspects.
   */
  private UncaughtExceptionHandler defaultExceptionHandler = new UncaughtExceptionHandler() {
    public void uncaughtException(Thread t, Throwable e) {
      logger.severe("A non-test thread threw an uncaught exception. This" +
      		" should never happen in normal circumstances: report to " +
          RandomizedRunner.class.getName() + " developers. Thread: " +
      		t + ", exception: " + e.toString() + ", stack:\n" + formatStackTrace(e.getStackTrace()));
    }
  };

  /** Creates a new runner for the given class. */
  public RandomizedRunner(Class<?> testClass) throws InitializationError {
    this.suiteClass = testClass;
    this.allTargetMethods = immutableCopy(sort(allDeclaredMethods(suiteClass)));

    // Initialize the runner's master seed/ randomness source.
    final long randomSeed = MurmurHash3.hash(sequencer.getAndIncrement() + System.nanoTime());
    final String globalSeed = System.getProperty(SYSPROP_RANDOM_SEED);
    if (globalSeed != null) {
      final long[] seedChain = parseSeedChain(globalSeed);
      if (seedChain.length == 0 || seedChain.length > 2) {
        throw new IllegalArgumentException("Invalid system property " 
            + SYSPROP_RANDOM_SEED + " specification: " + globalSeed);
      }

      if (seedChain.length > 1)
        testCaseRandomnessOverride = new Randomness(seedChain[1]);
      runnerRandomness = new Randomness(seedChain[0]);
    } else if (suiteClass.isAnnotationPresent(Seed.class)) {
      runnerRandomness = new Randomness(seedFromAnnot(suiteClass, randomSeed)[0]);
    } else {
      runnerRandomness = new Randomness(randomSeed);
    }

    // Iterations property is primary wrt to annotations, so we leave an "undefined" value as null.
    if (System.getProperty(SYSPROP_ITERATIONS) != null) {
      this.iterationsOverride = RandomizedTest.systemPropertyAsInt(SYSPROP_ITERATIONS, 0);
      if (iterationsOverride < 1)
        throw new IllegalArgumentException(
            "System property " + SYSPROP_ITERATIONS + " must be >= 1: " + iterationsOverride);
    } else {
      this.iterationsOverride = null;
    }

    this.killAttempts = RandomizedTest.systemPropertyAsInt(SYSPROP_KILLATTEMPTS, DEFAULT_KILLATTEMPTS);
    this.killWait = RandomizedTest.systemPropertyAsInt(SYSPROP_KILLWAIT, DEFAULT_KILLWAIT);
    this.timeoutOverride = RandomizedTest.systemPropertyAsInt(SYSPROP_TIMEOUT, DEFAULT_TIMEOUT);

    // TODO: should validation and everything else be done lazily after RunNotifier is available?

    // Fail fast if suiteClass is inconsistent or selected "standard" JUnit rules are somehow broken.
    validateTarget();

    // Collect all test candidates, regardless if they will be executed or not.
    suiteDescription = Description.createSuiteDescription(suiteClass);
    testCandidates = collectTestCandidates(suiteDescription);
  }

  /**
   * Return the current tree of test descriptions (filtered).
   */
  @Override
  public Description getDescription() {
    return suiteDescription;
  }

  /**
   * Implement {@link Filterable} because GUIs depend on it to run tests selectively.
   */
  @Override
  public void filter(Filter filter) throws NoTestsRemainException {
    this.filter = filter;
  }

  /**
   * Runs all tests and hooks.
   */
  @Override
  public void run(RunNotifier notifier) {
    runSuite(notifier);
  }

  /**
   * Test execution logic for the entire suite. 
   */
  private void runSuite(final RunNotifier notifier) {
    if (Thread.getDefaultUncaughtExceptionHandler() == null) {
      Thread.setDefaultUncaughtExceptionHandler(defaultExceptionHandler);
    }

    this.runnerThreadGroup = new RunnerThreadGroup(
        "RandomizedRunner " + Randomness.formatSeedChain(runnerRandomness));

    final Thread runner = new Thread(runnerThreadGroup, "main-" + Randomness.formatSeedChain(runnerRandomness)) {
      public void run() {
        RandomizedContext context = createContext(runnerThreadGroup);
        runSuite(context, notifier);
        context.dispose();
      }
    };

    runner.start();
    try {
      runner.join();
    } catch (InterruptedException e) {
      notifier.fireTestFailure(new Failure(suiteDescription, 
          new RuntimeException("Interrupted while waiting for the suite runner? Weird.", e)));
    }

    runnerThreadGroup = null;    
  }

  /**
   * Test execution logic for the entire suite, executing under designated
   * {@link RunnerThreadGroup}.
   */
  private void runSuite(final RandomizedContext context, final RunNotifier notifier) {
    context.push(runnerRandomness);
    try {
      // Check for automatically hookable listeners.
      subscribeListeners(notifier);
      
      // Validate suiteClass with custom validators.
      if (runCustomValidators(notifier)) {
        // Filter out test candidates to see if there's anything left. If not,
        // don't bother running class hooks.
        List<TestCandidate> filtered = getFilteredTestCandidates();
        if (!filtered.isEmpty()) {
          try {
            runBeforeClassMethods();

            for (final TestCandidate c : filtered) {
              final Runnable testRunner = new Runnable() {
                public void run() {
                  RandomizedContext current = RandomizedContext.current();
                  try {
                    current.push(c.randomness);
                    runSingleTest(notifier, c);
                  } catch (Throwable t) {
                    Rethrow.rethrow(augmentStackTrace(t));                    
                  } finally {
                    current.pop();
                  }
                }
              };

              // If the timeout is zero we'll need to wait for the test to terminate
              // anyway, so we just run it from the current thread. Otherwise we spawn
              // a child so that we can either kill it or abandon it. This is a bit harsh
              // on jvm resources, but will do for now.

              // This is also the place where we, theoretically at least, could spawn
              // multi-threaded tests. Simply by using executor service to run testRunners

              final int timeout = determineTimeout(c);
              if (timeout == 0) {
                testRunner.run();
              } else {
                runAndWait(notifier, c, testRunner, timeout);
              }

              notifier.fireTestFinished(c.description);
            }
          } catch (Throwable t) {
            if (t instanceof AssumptionViolatedException) {
              // Class level assumptions cause all tests to be ignored.
              // see Rants#RANT_3
              for (final TestCandidate c : filtered) {
                notifier.fireTestIgnored(c.description);
              }
              notifier.fireTestAssumptionFailed(new Failure(suiteDescription, t));
            } else {
              notifier.fireTestFailure(new Failure(suiteDescription, t));
            }
          }
  
          runAfterClassMethods(notifier);
        }
      }
    } catch (Throwable t) {
      notifier.fireTestFailure(new Failure(suiteDescription, t));
    } finally {
      // Clean up any threads left by hooks methods, but don't try to kill the zombies. 
      checkLeftOverThreads(notifier, suiteDescription, bulletProofZombies);
      unsubscribeListeners(notifier);
      context.pop();
    }    
  }

  /**
   * Run the provided <code>runnable</code> in a separate spawned
   * thread and wait for it to either complete execution or terminate
   * it prematurely if timeout expires, logging an exception.
   */
  private void runAndWait(RunNotifier notifier, TestCandidate c, Runnable runnable, int timeout) {
    Thread t = new Thread(runnable);
    try {
      t.start();
      t.join(timeout);
  
      if (t.isAlive()) {
        terminateAndFireFailure(t, notifier, c.description, "Test case thread timed out ");
        if (t.isAlive()) {
          bulletProofZombies.add(t);
        }
      }
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while waiting for worker? Weird.", e);
    }
  }

  /**
   * Runs a single test.
   */
  private void runSingleTest(RunNotifier notifier, final TestCandidate c) {
    notifier.fireTestStarted(c.description);
  
    if (isIgnored(c)) {
      notifier.fireTestIgnored(c.description);
      return;
    }
  
    Set<Thread> beforeTestSnapshot = threadsSnapshot();
    Object instance = null;
    try {
      // Get the test instance.
      instance = suiteClass.newInstance();

      // Run @Before hooks.
      for (Method m : getTargetMethods(Before.class))
        invoke(m, instance);
  
      // Collect rules and execute wrapped method.
      runWithRules(c, instance);
    } catch (Throwable e) {
      boolean isKilled = runnerThreadGroup.isKilled(Thread.currentThread());

      // Check if it's the runner trying to kill the thread. If so,
      // there is no point in reporting such an exception back. Also,
      // if the thread's been killed, we will not run any hooks (this is
      // pretty much a situation in which the world ends).
      if (isKilled && (e instanceof ThreadDeath)) {
        // TODO: System.exit() wouldn't run any post-cleanup on hooks. A better
        // way to resolve this would be to mark a global condition to ignore
        // all the remaining tests (fail with an assumption exception saying
        // there's a boogieman around or something).
        return;
      }

      // Augment stack trace and inject a fake stack entry with seed information.
      if (!isKilled) {
        e = augmentStackTrace(e);
        if (e instanceof AssumptionViolatedException) {
          notifier.fireTestAssumptionFailed(new Failure(c.description, e));
        } else {
          notifier.fireTestFailure(new Failure(c.description, e));
        }
      }
    }

    // Run @After hooks if an instance has been created.
    if (instance != null) {
      for (Method m : getTargetMethods(After.class)) {
        try {
          invoke(m, instance);
        } catch (Throwable t) {
          t = augmentStackTrace(t);
          notifier.fireTestFailure(new Failure(c.description, t));
        }
      }
    }
  
    // Check for run-away threads.
    bulletProofZombies.addAll(checkLeftOverThreads(notifier, c.description, beforeTestSnapshot));

    // Process uncaught exceptions, if any.
    runnerThreadGroup.processUncaught(notifier, c.description);
  }

  /**
   * Wrap with any rules the suiteClass has and execute as a {@link Statement}.
   */
  private void runWithRules(final TestCandidate c, final Object instance) throws Throwable {
    Statement s = new Statement() {
      public void evaluate() throws Throwable {
        invoke(c.method, instance);
      }
    };
    s = wrapMethodRules(s, c, instance);
    s.evaluate();
  }

  /**
   * Wrap the given statement in any declared MethodRules.
   */
  @SuppressWarnings("deprecation")
  private Statement wrapMethodRules(Statement s, TestCandidate c, Object instance) {
    TestClass info = new TestClass(suiteClass);
    FrameworkMethod fm = new FrameworkMethod(c.method);
    for (org.junit.rules.MethodRule each : 
        info.getAnnotatedFieldValues(suiteClass, Rule.class, org.junit.rules.MethodRule.class))
      s = each.apply(s, fm, instance);
    return s;
  }

  /**
   * Run before class methods. These fail immediately.
   */
  private void runBeforeClassMethods() throws Throwable {
    try {
      for (Method method : getTargetMethods(BeforeClass.class)) {
        invoke(method, null);
      }
    } catch (Throwable t) {
      throw augmentStackTraceNoContext(t, runnerRandomness);
    }
  }

  /**
   * Run after class methods. Collect exceptions, execute all.
   */
  private void runAfterClassMethods(RunNotifier notifier) {
    for (Method method : getTargetMethods(AfterClass.class)) {
      try {
        invoke(method, null);
      } catch (Throwable t) {
        t = augmentStackTraceNoContext(t, runnerRandomness);
        notifier.fireTestFailure(new Failure(suiteDescription, t));
      }
    }
  }

  /**
   * Create randomized context for the run. The context is shared by all
   * threads in a given thread group (but the source of {@link Randomness} 
   * is assigned per-thread).
   */
  private RandomizedContext createContext(ThreadGroup tg) {
    final boolean nightlyMode = RandomizedTest.systemPropertyAsBoolean(SYSPROP_NIGHTLY, false);
    return RandomizedContext.create(tg, suiteClass, runnerRandomness, nightlyMode);
  }

  /**
   * Attempt to terminate a given thread and log appropriate messages.
   */
  private void terminateAndFireFailure(Thread t, RunNotifier notifier, Description d, String msg) {
    StackTraceElement[] stackTrace = t.getStackTrace();

    RandomizedContext ctx = null; 
    try {
      ctx = RandomizedContext.context(t);
    } catch (IllegalStateException e) {
      logger.severe("No context information for this thread?: " + t);
    }

    tryToTerminate(t);

    State s = t.getState();
    String message = 
        msg +
        (s != State.TERMINATED ? " (and NOT TERMINATED, left in state " + s  + ")": " (and terminated)") +
        ": " + t.toString() +
        " (stack trace is a snapshot location).";

    ThreadingError ex = new ThreadingError(message);
    ex.setStackTrace(stackTrace);
    if (ctx != null) {
      ex = augmentStackTrace(ex);
    }
    notifier.fireTestFailure(new Failure(d, ex));    
  }

  /**
   * Try to terminate a given thread.
   */
  @SuppressWarnings("deprecation")
  private void tryToTerminate(Thread t) {
    if (!t.isAlive()) return;
  
    String tname = t.getName() + "(#" + System.identityHashCode(t) + ")";
  
    // We mark the thread as being killed because once we start calling
    // interrupt or stop weird things can happen. Any logged exceptions should
    // make it clear the thread is being killed.
    runnerThreadGroup.markAsBeingTerminated(t);

    logger.warning("Attempting to terminate thread: " + tname + ", currently at:\n"
        + formatStackTrace(t.getStackTrace()));
  
    // Try to interrupt first.
    int interruptAttempts = this.killAttempts;
    int interruptWait = this.killWait;
    do {
      try {
        t.interrupt();
        t.join(interruptWait);
      } catch (InterruptedException e) { /* ignore */ }
  
      if (!t.isAlive()) break;
      logger.fine("Trying to interrupt thread: " + tname 
          + ", retries: " + interruptAttempts + ", currently at: "
          + formatStackTrace(t.getStackTrace()));
    } while (--interruptAttempts >= 0);
  
    if (!t.isAlive()) {
      logger.warning("Interrupted a runaway thread: " + tname);
    }
  
    if (t.isAlive()) {
      logger.warning("Does not respond to interrupt(), trying to stop(): " + tname);
  
      // Try to sent ThreadDeath up its stack if interrupt is not working.
      int killAttempts = this.killAttempts;
      int killWait = this.killWait;
      do {
        try {
          t.stop();
          t.join(killWait);
        } catch (InterruptedException e) { /* ignore */ }
        if (!t.isAlive()) break;
        logger.fine("Trying to stop a runaway thread: " + tname 
            + ", retries: " + killAttempts + ", currently at: "
            + formatStackTrace(t.getStackTrace()));
      } while (--killAttempts >= 0);
  
      if (!t.isAlive()) {
          logger.warning("Stopped a runaway thread: " + tname);
      }      
    }
  
    if (t.isAlive()) {
      logger.severe("Could not interrupt or stop thread: " + tname);
    }
  }

  /**
   * Check for any left-over threads compared to expected state, notify
   * the runner about left-over threads and return the difference. 
   */
  private Set<Thread> checkLeftOverThreads(RunNotifier notifier, Description d, Set<Thread> expectedState) {
    final Set<Thread> now = threadsSnapshot();
    now.removeAll(expectedState);

    if (!now.isEmpty()) {
      for (Thread t : now) {
        terminateAndFireFailure(t, notifier, d, "Left-over thread detected ");
      }
    }

    return now;
  }

  /**
   * Run any {@link Validators} declared on the suite.
   */
  private boolean runCustomValidators(RunNotifier notifier) {
    for (Validators ann : getAnnotationsFromClassHierarchy(suiteClass, Validators.class)) {
      List<ClassValidator> validators = new ArrayList<ClassValidator>();
      try {
        for (Class<? extends ClassValidator> validatorClass : ann.value()) {
          try {
            validators.add(validatorClass.newInstance());
          } catch (Throwable t) {
            throw new RuntimeException("Could not initialize suite class: "
                + suiteClass.getName() + " because its @ClassValidators contains non-instantiable: "
                + validatorClass.getName(), t); 
          }
        }
  
        for (ClassValidator v : validators) {
            v.validate(suiteClass);
        }
      } catch (Throwable t) {
        notifier.fireTestFailure(new Failure(suiteDescription, t));
        return false;
      }
    }
    return true;
  }

  /** Subscribe annotation listeners to the notifier. */
  private void subscribeListeners(RunNotifier notifier) {
    for (Listeners ann : getAnnotationsFromClassHierarchy(suiteClass, Listeners.class)) {
      for (Class<? extends RunListener> clazz : ann.value()) {
        try {
          RunListener listener = clazz.newInstance();
          autoListeners.add(listener);
          notifier.addListener(listener);
        } catch (Throwable t) {
          throw new RuntimeException("Could not initialize suite class: "
              + suiteClass.getName() + " because its @Listener is not instantiable: "
              + clazz.getName(), t); 
        }
      }
    }
  }

  /** Unsubscribe listeners. */
  private void unsubscribeListeners(RunNotifier notifier) {
    for (RunListener r : autoListeners)
      notifier.removeListener(r);
  }

  /**
   * Apply filtering to candidates.
   */
  private List<TestCandidate> getFilteredTestCandidates() {
    // Check for class filter (most restrictive, immediate answer).
    if (System.getProperty(SYSPROP_TESTCLASS) != null) {
      if (!suiteClass.getName().equals(System.getProperty(SYSPROP_TESTCLASS))) {
        return Collections.emptyList();
      }
    }

    // Check for method filter, if defined.
    String methodFilter = System.getProperty(SYSPROP_TESTMETHOD);

    // Apply filters.
    List<TestCandidate> filtered = new ArrayList<TestCandidate>(testCandidates);
    for (Iterator<TestCandidate> i = filtered.iterator(); i.hasNext(); ) {
      final TestCandidate candidate = i.next();
      if (methodFilter != null && !methodFilter.equals(candidate.method.getName())) {
        i.remove();
      } else if (filter != null && !filter.shouldRun(candidate.description)) {
        i.remove();
      }
    }
    return filtered;
  }

  /** 
   * Returns true if we should ignore this test candidate.
   */
  private boolean isIgnored(final TestCandidate c) {
    if (c.method.getAnnotation(Ignore.class) != null)
      return true;

    if (!RandomizedContext.current().isNightly()) {
      if (c.method.getAnnotation(Nightly.class) != null ||
          suiteClass.getAnnotation(Nightly.class) != null) {
        return true;
      }
    }

    return false;
  }

  /**
   * Construct a list of ordered framework methods. Minor tweaks are done depending
   * on the annotation (reversing order, etc.). 
   */
  private List<Method> getTargetMethods(Class<? extends Annotation> ann) {
    List<List<Method>> list = mutableCopy(
        removeShadowed(removeOverrides(annotatedWith(allTargetMethods, ann))));

    // Reverse processing order to super...clazz for befores
    if (ann == Before.class || ann == BeforeClass.class) {
      Collections.reverse(list);
    }

    // Shuffle at class level.
    Random rnd = new Random(runnerRandomness.seed);
    for (List<Method> clazzLevel : list) {
      Collections.shuffle(clazzLevel, rnd);
    }

    return flatten(list);
  }

  /**
   * Collect all test candidates, regardless if they will be executed or not. At this point
   * individual test methods are also expanded into multiple executions corresponding
   * to the number of iterations ({@link #SYSPROP_ITERATIONS}) and the initial method seed 
   * is preassigned. 
   * 
   * <p>The order of test candidates is shuffled based on the runner's random.</p> 
   * 
   * @see Rants#RANT_1
   */
  private List<TestCandidate> collectTestCandidates(Description classDescription) {
    List<Method> testMethods = 
        new ArrayList<Method>(
            flatten(removeOverrides(annotatedWith(allTargetMethods, Test.class))));

    // Shuffle at real test-case level, don't shuffle iterations or explicit @Seeds order.
    Collections.shuffle(testMethods, new Random(runnerRandomness.seed));

    List<TestCandidate> candidates = new ArrayList<TestCandidate>();
    for (Method method : testMethods) {
      Description parent = classDescription;
      int methodIterations = determineMethodIterationCount(method);
      if (methodIterations > 1) {
        // This will be un-clickable in Eclipse. See Rants.
        parent = Description.createSuiteDescription(method.getName());
        classDescription.addChild(parent);
      }

      for (final long testSeed : determineMethodSeeds(method)) {
        final boolean fixedSeed = isConstantSeedForAllIterations(method);
  
        // Create test iterations.
        for (int i = 0; i < methodIterations; i++) {
          final long iterSeed = (fixedSeed ? testSeed : testSeed ^ MurmurHash3.hash((long) i));        
          Randomness iterRandomness = new Randomness(iterSeed);
  
          // Create a description that contains everything we need to know to repeat the test.
          Description description = 
              Description.createSuiteDescription(
                  method.getName() +
                  (methodIterations > 1 ? "#" + i : "") +
                  " " + formatSeedChain(runnerRandomness, iterRandomness) + 
                  "(" + suiteClass.getName() + ")");
  
          // Add the candidate.
          parent.addChild(description);
          candidates.add(new TestCandidate(method, iterRandomness, description));
        }
      }
    }
    return candidates;
  }

  /**
   * Determine if a given method's iterations should run with a fixed seed or not.
   */
  private boolean isConstantSeedForAllIterations(Method method) {
    if (testCaseRandomnessOverride != null)
      return true;

    Repeat repeat;
    if ((repeat = method.getAnnotation(Repeat.class)) != null) {
      return repeat.useConstantSeed();
    }
    if ((repeat = suiteClass.getAnnotation(Repeat.class)) != null) {
      return repeat.useConstantSeed();
    }
    
    return false;
  }

  /**
   * Determine method iteration count based on (first declaration order wins):
   * <ul>
   *  <li>global property {@link #SYSPROP_ITERATIONS}.</li>
   *  <li>method annotation {@link Repeat}.</li>
   *  <li>class annotation {@link Repeat}.</li>
   *  <li>The default (1).</li>
   * <ul>
   */
  private int determineMethodIterationCount(Method method) {
    // Global override.
    if (iterationsOverride != null)
      return iterationsOverride;

    Repeat repeat;
    if ((repeat = method.getAnnotation(Repeat.class)) != null) {
      return repeat.iterations();
    }
    if ((repeat = suiteClass.getAnnotation(Repeat.class)) != null) {
      return repeat.iterations();
    }

    return DEFAULT_ITERATIONS;
  }

  /**
   * Determine a given method's initial random seed.
   * 
   * @see Seed
   * @see Seeds
   */
  private long [] determineMethodSeeds(Method method) {
    if (testCaseRandomnessOverride != null) {
      return new long [] { testCaseRandomnessOverride.seed };
    }

    // We assign each method a different starting hash based on the global seed
    // and a hash of their name (so that the order of methods does not matter, only
    // their names). Take into account global override and method and class level
    // {@link Seed} annotations.    
    final long randomSeed = 
        runnerRandomness.seed ^ MurmurHash3.hash((long) method.getName().hashCode());
    final HashSet<Long> seeds = new HashSet<Long>();

    // Check method-level @Seed and @Seeds annotation first. 
    // They take precedence over anything else.
    Seed seed;
    if ((seed = method.getAnnotation(Seed.class)) != null) {
      for (long s : seedFromAnnot(method, randomSeed)) {
        seeds.add(s);
      }
    }

    // Check a number of seeds on a single method.
    if (method.isAnnotationPresent(Seeds.class)) {
      for (Seed s : method.getAnnotation(Seeds.class).value()) {
        if (s.value().equals("random"))
          seeds.add(randomSeed);
        else {
          for (long s2 : parseSeedChain(s.value())) {
            seeds.add(s2);
          }
        }
      }
    }

    // Check suite-level override.
    if (seeds.isEmpty()) {
      if ((seed = suiteClass.getAnnotation(Seed.class)) != null) {
        if (!seed.value().equals("random")) {
          long [] seedChain = parseSeedChain(suiteClass.getAnnotation(Seed.class).value());
          if (seedChain.length > 1)
            seeds.add(seedChain[1]);
        }
      }
    }

    // If still empty, add the derived random seed.
    if (seeds.isEmpty()) {
      seeds.add(randomSeed);
    }

    long [] result = new long [seeds.size()];
    int i = 0;
    for (Long s : seeds) {
      result[i++] = s;
    }
    return result;
  }

  /**
   * Determine timeout for a given test candidate.
   */
  private int determineTimeout(TestCandidate c) {
    // initial value
    int timeout = this.timeoutOverride;
    
    // Class-override.
    Timeout timeoutAnn = c.method.getDeclaringClass().getAnnotation(Timeout.class);
    if (timeoutAnn != null) {
      timeout = timeoutAnn.millis();
    }
    
    // Method-override.
    timeoutAnn = c.method.getAnnotation(Timeout.class);
    if (timeoutAnn != null) {
      timeout = timeoutAnn.millis();
    }
  
    return timeout;
  }

  /**
   * Invoke a given method on a suiteClass instance (can be null for static methods).
   */
  private void invoke(Method m, Object instance, Object... args) throws Throwable {
    if (!Modifier.isPublic(m.getModifiers())) {
      try {
        if (!m.isAccessible()) {
          m.setAccessible(true);
        }
      } catch (SecurityException e) {
        throw new RuntimeException("There is a non-public hook method. This requires " +
            "ReflectPermission('suppressAccessChecks'). Don't run with the security manager or " +
            " add this permission to the runner. Offending method: " + m.toGenericString());
      }
    }

    try {
      m.invoke(instance, args);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  /**
   * Validate methods and hooks in the suiteClass. Follows "standard" JUnit rules,
   * with some exceptions on return values and more rigorous checking of shadowed
   * methods and fields.
   */
  private void validateTarget() {
    // Target is accessible (public, concrete, has a parameterless constructor etc).
    Validation.checkThat(suiteClass)
      .describedAs("Suite class " + suiteClass.getName())
      .isPublic()
      .isConcreteClass()
      .hasPublicNoArgsConstructor();

    // @BeforeClass
    for (Method method : flatten(annotatedWith(allTargetMethods, BeforeClass.class))) {
      Validation.checkThat(method)
        .describedAs("@BeforeClass method " + suiteClass.getName() + "#" + method.getName())
        // .isPublic() // Intentional, you can hide it from subclasses.
        .isStatic()
        .hasArgsCount(0);
    }

    // @AfterClass
    for (Method method : flatten(annotatedWith(allTargetMethods, AfterClass.class))) {
      Validation.checkThat(method)
        .describedAs("@AfterClass method " + suiteClass.getName() + "#" + method.getName())
        // .isPublic() // Intentional, you can hide it from subclasses.
        .isStatic()
        .hasArgsCount(0);
    }

    // @Before
    for (Method method : flatten(annotatedWith(allTargetMethods, Before.class))) {
      Validation.checkThat(method)
        .describedAs("@Before method " + suiteClass.getName() + "#" + method.getName())
        // .isPublic()  // Intentional, you can hide it from subclasses.
        .isNotStatic()
        .hasArgsCount(0);
    }

    // @After
    for (Method method : flatten(annotatedWith(allTargetMethods, After.class))) {
      Validation.checkThat(method)
        .describedAs("@After method " + suiteClass.getName() + "#" + method.getName())
        // .isPublic()  // Intentional, you can hide it from subclasses.
        .isNotStatic()
        .hasArgsCount(0);
    }

    // @Test methods
    for (Method method : flatten(annotatedWith(allTargetMethods, Test.class))) {
      Validation.checkThat(method)
        .describedAs("Test method " + suiteClass.getName() + "#" + method.getName())
        .isPublic()
        .isNotStatic()
        .hasArgsCount(0);

      // @Seed annotation on test methods must have at most 1 seed value.
      if (method.isAnnotationPresent(Seed.class)) {
        try {
          String seedChain = method.getAnnotation(Seed.class).value();
          if (!seedChain.equals("random")) {
            long[] chain = Randomness.parseSeedChain(seedChain);
            if (chain.length > 1) {
              throw new IllegalArgumentException("@Seed on methods must contain one seed only (no runner seed).");
            }
          }
        } catch (IllegalArgumentException e) {
          throw new RuntimeException("@Seed annotation invalid on method "
              + method.getName() + ", in class " + suiteClass.getName() + ": "
              + e.getMessage());
        }
      }
    }

    // TODO: Validate @Rule fields (what are the "rules" for these anyway?)
  }

  /**
   * Augment stack trace of the given exception with seed infos.
   */
  private static <T extends Throwable> T augmentStackTraceNoContext(T e, Randomness... seeds) {
    List<StackTraceElement> stack = new ArrayList<StackTraceElement>(
        Arrays.asList(e.getStackTrace()));
  
    stack.add(0,  new StackTraceElement(AUGMENTED_SEED_PACKAGE + ".SeedInfo", 
        "seed", Randomness.formatSeedChain(seeds), 0));
  
    e.setStackTrace(stack.toArray(new StackTraceElement [stack.size()]));
  
    return e;
  }
  
  /**
   * Augment stack trace of the given exception with seed infos from the
   * current thread's randomized context.
   */
  static <T extends Throwable> T augmentStackTrace(T e) {
    RandomizedContext context = RandomizedContext.current();
    return augmentStackTraceNoContext(e, context.getRandomnesses());
  }

  /** Format a list of stack entries into a string. */
  private static String formatStackTrace(StackTraceElement[] stackTrace) {
    StringBuilder b = new StringBuilder();
    for (StackTraceElement e : stackTrace) {
      b.append("    ").append(e.toString()).append("\n");
    }
    return b.toString();
  }

  /**
   * Return an estimated set of current thread group's 
   * live threads, excluding the current thread.
   */
  private static Set<Thread> threadsSnapshot() {
    final Thread current = Thread.currentThread();
    final ThreadGroup tg = current.getThreadGroup();
  
    Thread [] list;
    do {
      list = new Thread [tg.activeCount() + /* padding to detect overflow */ 5];
      tg.enumerate(list);
    } while (list[list.length - 1] != null);
  
    final HashSet<Thread> result = new HashSet<Thread>();
    for (Thread t : list) {
      if (t != null && t != current) 
        result.add(t);
    }
  
    return result;
  }

  /**
   * Collect all annotations from a clazz hierarchy. Superclass's annotations come first. 
   * {@link Inherited} annotations are removed (hopefully, the spec. isn't clear on this whether
   * the same object is returned or not for inherited annotations).
   */
  private static <T extends Annotation> List<T> getAnnotationsFromClassHierarchy(Class<?> clazz, Class<T> annotation) {
    List<T> anns = new ArrayList<T>();
    IdentityHashMap<T,T> inherited = new IdentityHashMap<T,T>();
    for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass()) {
      if (c.isAnnotationPresent(annotation)) {
        T ann = c.getAnnotation(annotation);
        if (ann.annotationType().isAnnotationPresent(Inherited.class) && 
            inherited.containsKey(ann)) {
            continue;
        }
        anns.add(ann);
        inherited.put(ann, ann);
      }
    }

    Collections.reverse(anns);
    return anns;
  }

  /**
   * Get an annotated element's {@link Seed} annotation and determine if it's fixed
   * or not. If it is fixed, return the seeds. Otherwise return <code>randomSeed</code>.
   */
  private long [] seedFromAnnot(AnnotatedElement element, long randomSeed) {
    Seed seed = element.getAnnotation(Seed.class);
    String seedChain = seed.value();
    if (seedChain.equals("random")) {
      return new long [] { randomSeed };
    }
  
    return parseSeedChain(seedChain);
  }

  /**
   * {@link RandomizedRunner} augments stack traces of test methods that ended in an exception
   * and inserts a fake entry starting with {@link #AUGMENTED_SEED_PACKAGE}.
   * 
   * @return A string is returned with seeds combined, if any. Null is returned if no augmentation
   * can be found. 
   */
  public static String extractSeed(Throwable t) {
    StringBuilder b = new StringBuilder();
    while (t != null) {
      for (StackTraceElement s : t.getStackTrace()) {
        if (s.getClassName().startsWith(AUGMENTED_SEED_PACKAGE)) {
          if (b.length() > 0) b.append(", ");
          b.append(s.getFileName());
        }
      }
      t = t.getCause();
    }

    if (b.length() == 0)
      return null;
    else
      return b.toString();
  }

  /**
   * Strip the seed and round number appended to a method name in test runs
   * (because there is no other place we can append it to). 
   */
  public static String stripSeed(String methodName) {
    return methodName.replaceAll("(\\#[0-9+])?\\s\\[[A-Za-z0-9\\:]+\\]", "");
  }
}
