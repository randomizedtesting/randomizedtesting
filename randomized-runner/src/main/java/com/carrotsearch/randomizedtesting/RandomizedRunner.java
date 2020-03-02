package com.carrotsearch.randomizedtesting;

import static com.carrotsearch.randomizedtesting.SysGlobals.*;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Assert;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import com.carrotsearch.randomizedtesting.ClassModel.MethodModel;
import com.carrotsearch.randomizedtesting.annotations.Listeners;
import com.carrotsearch.randomizedtesting.annotations.Name;
import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Seed;
import com.carrotsearch.randomizedtesting.annotations.SeedDecorators;
import com.carrotsearch.randomizedtesting.annotations.Seeds;
import com.carrotsearch.randomizedtesting.annotations.TestCaseInstanceProvider;
import com.carrotsearch.randomizedtesting.annotations.TestCaseOrdering;
import com.carrotsearch.randomizedtesting.annotations.TestContextRandomSupplier;
import com.carrotsearch.randomizedtesting.annotations.TestMethodProviders;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakAction;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakFilters;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakGroup;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakLingering;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakZombies;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakZombies.Consequence;
import com.carrotsearch.randomizedtesting.annotations.Timeout;
import com.carrotsearch.randomizedtesting.annotations.TimeoutSuite;
import com.carrotsearch.randomizedtesting.rules.StatementAdapter;

/**
 * A {@link Runner} implementation for running randomized test cases with 
 * predictable and repeatable randomness.
 * 
 * <p>Supports the following JUnit4 features:
 * <ul>
 *   <li>{@link BeforeClass}-annotated methods (before all tests of a class/superclass),</li>
 *   <li>{@link Before}-annotated methods (before each test),</li>
 *   <li>{@link Test}-annotated methods,</li>
 *   <li>{@link After}-annotated methods (after each test),</li>
 *   <li>{@link AfterClass}-annotated methods (after all tests of a class/superclass),</li>
 *   <li>{@link Rule}-annotated fields implementing {@link org.junit.rules.MethodRule} 
 *       and {@link TestRule}.</li>
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
 *   <li>a test method must not leave behind any active threads; this is detected
 *       using {@link ThreadGroup} active counts and is sometimes problematic (many classes
 *       in the standard library leave active threads behind without waiting for them to terminate).
 *       One can use the {@link ThreadLeakScope}, {@link ThreadLeakAction}
 *       and other annotations to control how aggressive the detection
 *       strategy is and if it fails the test or not.</li>
 *   <li>uncaught exceptions from any of children threads will cause the test to fail.</li>
 * </ul>
 * 
 * @see RandomizedTest
 * @see ThreadLeakAction
 * @see ThreadLeakScope
 * @see ThreadLeakZombies
 * @see ThreadLeakGroup
 * @see ThreadLeakLingering
 * @see ThreadLeakFilters
 * @see Listeners
 * @see RandomizedContext
 * @see TestMethodProviders
 */
public final class RandomizedRunner extends Runner implements Filterable {
  /**
   * Fake package of a stack trace entry inserted into exceptions thrown by 
   * test methods. These stack entries contain additional information about
   * seeds used during execution. 
   */
  public static final String AUGMENTED_SEED_PACKAGE = "__randomizedtesting";

  /**
   * Default timeout for a single test case. By default
   * the timeout is <b>disabled</b>. Use global system property
   * {@link SysGlobals#SYSPROP_TIMEOUT} or an annotation {@link Timeout} if you need to set
   * timeouts or expect some test cases may hang. This will slightly slow down
   * the tests because each test case is executed in a forked thread.
   *
   * @see SysGlobals#SYSPROP_TIMEOUT() 
   */
  public static final int DEFAULT_TIMEOUT = 0;

  /**
   * Default timeout for an entire suite. By default
   * the timeout is <b>disabled</b>. Use the global system property
   * {@link SysGlobals#SYSPROP_TIMEOUT_SUITE} or an annotation {@link TimeoutSuite} 
   * if you need to set
   * timeouts or expect some tests (hooks) may hang.
   *
   * @see SysGlobals#SYSPROP_TIMEOUT_SUITE() 
   */
  public static final int DEFAULT_TIMEOUT_SUITE = 0;

  /**
   * The default number of first interrupts, then Thread.stop attempts.
   */
  public static final int DEFAULT_KILLATTEMPTS = 5;
  
  /**
   * Time in between interrupt retries or stop retries.
   */
  public static final int DEFAULT_KILLWAIT = 500;

  /**
   * The default number of test repeat iterations.
   */
  public static final int DEFAULT_ITERATIONS = 1;

  /**
   * Test candidate (model).
   */
  class TestCandidate {
    public final long seed;
    public final Description description;
    public final Method method;
    public final InstanceProvider instanceProvider;

    public TestCandidate(Method method, long seed, Description description, InstanceProvider instanceProvider) {
      this.seed = seed;
      this.description = description;
      this.method = method;
      this.instanceProvider = instanceProvider;
    }

    public Class<?> getTestClass() {
      return suiteClass;
    }
  }

  /**
   * Package scope logger. 
   */
  final static Logger logger = Logger.getLogger(RandomizedRunner.class.getSimpleName());

  /** 
   * A sequencer for affecting the initial seed in case of rapid succession of this class
   * instance creations. Not likely, but can happen two could get the same seed.
   */
  private final static AtomicLong sequencer = new AtomicLong();

  private static final List<String> DEFAULT_STACK_FILTERS = Arrays.asList(new String [] {
      "org.junit.",
      "junit.framework.",
      "sun.",
      "java.lang.reflect.",
      "com.carrotsearch.randomizedtesting.",
  });

  /** The class with test methods (suite). */
  private final Class<?> suiteClass;

  /** The runner's seed (master). */
  final Randomness runnerRandomness;

  /** 
   * If {@link SysGlobals#SYSPROP_RANDOM_SEED} property is used with two arguments (master:method)
   * then this field contains method-level override. 
   */
  private Randomness testCaseRandomnessOverride;

  /** 
   * The number of each test's randomized iterations.
   * 
   * @see SysGlobals#SYSPROP_ITERATIONS
   */
  private final Integer iterationsOverride;

  /** All test candidates, processed (seeds assigned) and flattened. */
  private List<TestCandidate> testCandidates;

  /** Class suite description. */
  private Description suiteDescription;

  /**
   * All tests are executed under a specified thread group so that we can have some control
   * over how many threads have been started/ stopped. System daemons shouldn't be under
   * this group.
   */
  RunnerThreadGroup runnerThreadGroup;

  /** 
   * @see #subscribeListeners(RunNotifier) 
   */
  private final List<RunListener> autoListeners = new ArrayList<RunListener>();

  /**
   * @see SysGlobals#SYSPROP_APPEND_SEED
   */
  private boolean appendSeedParameter;

  /**
   * Stack trace filtering/ dumping.
   */
  private final TraceFormatting traces;

  /**
   * The container we're running in.
   */
  private RunnerContainer containerRunner;

  /**
   * {@link UncaughtExceptionHandler} for capturing uncaught exceptions
   * from the test group and globally.
   */
  QueueUncaughtExceptionsHandler handler;

  /**
   * Class model.
   */
  private ClassModel classModel;

  /**
   * Random class implementation supplier.
   */
  private final RandomSupplier randomSupplier;
  
  /**
   * Methods cache.
   */
  private Map<Class<? extends Annotation>,List<Method>> shuffledMethodsCache = new HashMap<Class<? extends Annotation>,List<Method>>();

  /**
   * A marker for flagging zombie threads (leaked threads that couldn't be killed).
   */
  static AtomicBoolean zombieMarker = new AtomicBoolean(false);

  /**
   * The "main" thread group we will be tracking (including subgroups).
   */
  final static ThreadGroup mainThreadGroup = Thread.currentThread().getThreadGroup();

  private final Map<String,String> restoreProperties = new HashMap<String,String>();

  public GroupEvaluator groupEvaluator;

  /** Creates a new runner for the given class. */
  public RandomizedRunner(Class<?> testClass) throws InitializationError {
    appendSeedParameter = RandomizedTest.systemPropertyAsBoolean(SYSPROP_APPEND_SEED(), false);
    
    if (RandomizedTest.systemPropertyAsBoolean(SYSPROP_STACKFILTERING(), true)) {
      this.traces = new TraceFormatting(DEFAULT_STACK_FILTERS);
    } else {
      this.traces = new TraceFormatting();
    }

    this.suiteClass = testClass;
    this.classModel = new ClassModel(testClass);

    // Try to detect the JUnit runner's container. This changes reporting 
    // behavior slightly.
    this.containerRunner = detectContainer();

    // Initialize the runner's master seed/ randomness source.
    {
      List<SeedDecorator> decorators = new ArrayList<SeedDecorator>();
      for (SeedDecorators decAnn : getAnnotationsFromClassHierarchy(testClass, SeedDecorators.class)) {
        for (Class<? extends SeedDecorator> clazz : decAnn.value()) {
          try {
            SeedDecorator dec = clazz.newInstance();
            dec.initialize(testClass);
            decorators.add(dec);
          } catch (Throwable t) {
            throw new RuntimeException("Could not initialize suite class: "
                + testClass.getName() + " because its @SeedDecorators contains non-instantiable: "
                + clazz.getName(), t); 
          }
        }
      }
      SeedDecorator[] decArray = decorators.toArray(new SeedDecorator [decorators.size()]);

      randomSupplier = determineRandomSupplier(testClass);

      final long randomSeed = MurmurHash3.hash(sequencer.getAndIncrement() + System.nanoTime());
      final String globalSeed = emptyToNull(System.getProperty(SYSPROP_RANDOM_SEED()));
      final long initialSeed;
      if (globalSeed != null) {
        final long[] seedChain = SeedUtils.parseSeedChain(globalSeed);
        if (seedChain.length == 0 || seedChain.length > 2) {
          throw new IllegalArgumentException("Invalid system property " 
              + SYSPROP_RANDOM_SEED() + " specification: " + globalSeed);
        }

        if (seedChain.length > 1) {
          testCaseRandomnessOverride = new Randomness(seedChain[1], randomSupplier);
        }

        initialSeed = seedChain[0];
      } else if (suiteClass.isAnnotationPresent(Seed.class)) {
        initialSeed = seedFromAnnot(suiteClass, randomSeed)[0];
      } else {
        initialSeed = randomSeed;
      }
      runnerRandomness = new Randomness(initialSeed, randomSupplier, decArray);
    }

    // Iterations property is primary wrt to annotations, so we leave an "undefined" value as null.
    if (emptyToNull(System.getProperty(SYSPROP_ITERATIONS())) != null) {
      this.iterationsOverride = RandomizedTest.systemPropertyAsInt(SYSPROP_ITERATIONS(), 0);
      if (iterationsOverride < 1)
        throw new IllegalArgumentException(
            "System property " + SYSPROP_ITERATIONS() + " must be >= 1: " + iterationsOverride);
    } else {
      this.iterationsOverride = null;
    }
    
    try {
      // Fail fast if suiteClass is inconsistent or selected "standard" JUnit rules are somehow broken.
      validateTarget();
  
      // Collect all test candidates, regardless if they will be executed or not.
      suiteDescription = Description.createSuiteDescription(suiteClass);
      testCandidates = collectTestCandidates(suiteDescription);
      this.groupEvaluator = new GroupEvaluator(testCandidates);

      // GH-251: Apply suite and test filters early so that the returned Description gets updated.
      if (emptyToNull(System.getProperty(SYSPROP_TESTMETHOD())) != null) {
        try {
          filter(new MethodGlobFilter(System.getProperty(SYSPROP_TESTMETHOD())));
        } catch (NoTestsRemainException e) {
          // Ignore the exception in the constructor.
        }
      }
      
      if (emptyToNull(System.getProperty(SYSPROP_TESTCLASS())) != null) {
        Filter suiteFilter = new ClassGlobFilter(System.getProperty(SYSPROP_TESTCLASS()));
        if (!suiteFilter.shouldRun(suiteDescription)) {
          suiteDescription.getChildren().clear();
          testCandidates.clear();
        }
      }
    } catch (Throwable t) {
      throw new InitializationError(t);
    }
  }

  private RandomSupplier determineRandomSupplier(Class<?> testClass) {
    List<TestContextRandomSupplier> randomImpl = getAnnotationsFromClassHierarchy(testClass, TestContextRandomSupplier.class);
    if (randomImpl.size() == 0) {
      return RandomSupplier.DEFAULT;
    } else {
      Class<? extends RandomSupplier> clazz = randomImpl.get(randomImpl.size() - 1).value();
      try {
        return clazz.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        throw new IllegalArgumentException("Could not instantiate random supplier of class: " + clazz, e);
      }
    }
  }

  /**
   * Attempt to detect the container we're running under. 
   */
  private static RunnerContainer detectContainer() {
    StackTraceElement [] stack = Thread.currentThread().getStackTrace();
    if (stack.length > 0) {
      String topClass = stack[stack.length - 1].getClassName();

      if (topClass.equals("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner")) {
        return RunnerContainer.ECLIPSE;
      }

      if (topClass.startsWith("com.intellij.")) {
        return RunnerContainer.IDEA;
      }
    }
    return RunnerContainer.UNKNOWN;
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
    // Apply the filter to test candidates.
    testCandidates = applyFilters(suiteClass, testCandidates, Collections.singleton(filter));

    // Prune any removed tests from the already created Descriptions
    // and prune any empty resulting suites. 
    Set<Description> descriptions = Collections.newSetFromMap(new IdentityHashMap<Description, Boolean>());
    for (TestCandidate tc : testCandidates) {
      descriptions.add(tc.description);
    }

    suiteDescription = prune(suiteDescription, descriptions);

    if (testCandidates.isEmpty()) {
      throw new NoTestsRemainException();
    }
  }

  private static Description prune(Description suite, Set<Description> permitted) {
    if (suite.isSuite()) {
      ArrayList<Description> children = suite.getChildren();
      ArrayList<Description> retained = new ArrayList<>(children.size());
      for (Description child : children) {
        if (child.isSuite()) {
          final Description description = prune(child, permitted);
          if (!child.getChildren().isEmpty()) {
            retained.add(description);
          }
        } else if (permitted.contains(child)) {
          retained.add(child);
        }
      }

      final Description suiteDescription = suite.childlessCopy();
      for (Description description : retained) {
        suiteDescription.addChild(description);
      }
      return suiteDescription;
    }
    return suite;
  }

  /**
   * Runs all tests and hooks.
   */
  @Override
  public void run(RunNotifier notifier) {
    processSystemProperties();
    try {
      runSuite(notifier);
    } finally {
      restoreSystemProperties();
    }
  }

  private void restoreSystemProperties() {
    for (Map.Entry<String,String> e : restoreProperties.entrySet()) {
      try {
        if (e.getValue() == null) {
          System.clearProperty(e.getKey());
        } else {
          System.setProperty(e.getKey(), e.getValue());
        }
      } catch (SecurityException x) {
        logger.warning("Could not restore system property: " + e.getKey() + " => " + e.getValue());
      }
    }
  }

  private void processSystemProperties() {
    try {
      String jvmCount = System.getProperty(SysGlobals.CHILDVM_SYSPROP_JVM_COUNT);
      String jvmId = System.getProperty(SysGlobals.CHILDVM_SYSPROP_JVM_ID);
      if (emptyToNull(jvmCount) == null &&
          emptyToNull(jvmId) == null) {
          // We don't run under JUnit4 so we have to fill in these manually.
          System.setProperty(SysGlobals.CHILDVM_SYSPROP_JVM_COUNT, "1");
          System.setProperty(SysGlobals.CHILDVM_SYSPROP_JVM_ID, "0");
          restoreProperties.put(SysGlobals.CHILDVM_SYSPROP_JVM_COUNT, jvmCount);
          restoreProperties.put(SysGlobals.CHILDVM_SYSPROP_JVM_ID, jvmId);
      }
    } catch (SecurityException e) {
      // Ignore if we can't set those properties.
      logger.warning("Could not set child VM count and ID properties.");
    }
  }

  static class UncaughtException {
    final Thread thread;
    final String threadName;
    final Throwable error;

    UncaughtException(Thread t, Throwable error) {
      this.threadName = Threads.threadName(t);
      this.thread = t;
      this.error = error;
    }
  }

  /**
   * Queue uncaught exceptions.
   */
  static class QueueUncaughtExceptionsHandler implements UncaughtExceptionHandler {
    private final ArrayList<UncaughtException> uncaughtExceptions = new ArrayList<UncaughtException>();
    private boolean reporting = true;

    @Override
    public void uncaughtException(Thread t, Throwable e) {
      synchronized (this) {
        if (!reporting) {
          return;
        }
        uncaughtExceptions.add(new UncaughtException(t, e));
      }

      Logger.getLogger(RunnerThreadGroup.class.getSimpleName()).log(
          Level.WARNING,
          "Uncaught exception in thread: " + t, e);
    }

    /**
     * Stop reporting uncaught exceptions.
     */
    void stopReporting() {
      synchronized (this) {
        reporting = false;
      }
    }

    /**
     * Resume uncaught exception reporting.
     */
    void resumeReporting() {
      synchronized (this) {
        reporting = true;
      }
    }

    /**
     * Return the current list of uncaught exceptions and clear it.
     */
    public List<UncaughtException> getUncaughtAndClear() {
      synchronized (this) {
        final ArrayList<UncaughtException> copy = new ArrayList<UncaughtException>(uncaughtExceptions);
        uncaughtExceptions.clear();
        return copy;
      }
    }
  }

  /**
   * Test execution logic for the entire suite. 
   */
  private void runSuite(final RunNotifier notifier) {
    // NOTE: this effectively means we can't run concurrent randomized runners.
    final UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
    handler = new QueueUncaughtExceptionsHandler();
    AccessController.doPrivileged(new PrivilegedAction<Void>() {
      @Override
      public Void run() {
        Thread.setDefaultUncaughtExceptionHandler(handler);
        return null;
      }
    });

    this.runnerThreadGroup = new RunnerThreadGroup(
        "TGRP-" + Classes.simpleName(suiteClass));

    final Thread runner = new Thread(runnerThreadGroup,
        "SUITE-" + Classes.simpleName(suiteClass) + "-seed#" + SeedUtils.formatSeedChain(runnerRandomness)) {
      public void run() {
        try {
          // Make sure static initializers are invoked and that they are invoked outside of
          // the randomized context scope. This is for consistency so that we're not relying
          // on the class NOT being initialized before.
          try {
            Class.forName(suiteClass.getName(), true, suiteClass.getClassLoader());
          } catch (ExceptionInInitializerError e) {
            throw e.getCause();
          }

          RandomizedContext context = createContext(runnerThreadGroup);
          runSuite(context, notifier);
          context.dispose();
        } catch (Throwable t) {
          notifier.fireTestFailure(new Failure(suiteDescription, t));
        }
      }
    };

    runner.start();
    try {
      runner.join();
    } catch (InterruptedException e) {
      notifier.fireTestFailure(new Failure(suiteDescription, 
          new RuntimeException("Interrupted while waiting for the suite runner? Weird.", e)));
    }

    UncaughtExceptionHandler current = Thread.getDefaultUncaughtExceptionHandler();
    if (current != handler) {
      notifier.fireTestFailure(new Failure(suiteDescription, 
          new RuntimeException("Suite replaced Thread.defaultUncaughtExceptionHandler. " +
          		"It's better not to touch it. Or at least revert it to what it was before. Current: " + 
              (current == null ? "(null)" : current.getClass()))));
    }

    AccessController.doPrivileged(new PrivilegedAction<Void>() {
      @Override
      public Void run() {
        Thread.setDefaultUncaughtExceptionHandler(previous);
        return null;
      }
    });
    runnerThreadGroup = null;
    handler = null;
  }

  /**
   * Test execution logic for the entire suite, executing under designated
   * {@link RunnerThreadGroup}.
   */
  private void runSuite(final RandomizedContext context, final RunNotifier notifier) {
    final Result result = new Result();
    final RunListener accounting = result.createListener();
    notifier.addListener(accounting);

    final Randomness classRandomness = runnerRandomness.clone(Thread.currentThread());
    context.push(classRandomness);
    try {
      // Check for automatically hookable listeners.
      subscribeListeners(notifier);

      // Fire a synthetic "suite started" event.
      for (RunListener r : autoListeners) { 
        try {
          r.testRunStarted(suiteDescription);
        } catch (Throwable e) {
          logger.log(Level.SEVERE, "Panic: RunListener hook shouldn't throw exceptions.", e);
        }
      }

      final List<TestCandidate> tests = testCandidates;
      if (!tests.isEmpty()) {
        Map<TestCandidate, Boolean> ignored = determineIgnoredTests(tests);

        if (ignored.size() == tests.size()) {
          // All tests ignored, ignore class hooks but report all the ignored tests.
          for (TestCandidate c : tests) {
            if (ignored.get(c)) {
              reportAsIgnored(notifier, groupEvaluator, c);
            }
          }
        } else {
          ThreadLeakControl threadLeakControl = new ThreadLeakControl(notifier, this);
          Statement s = runTestsStatement(threadLeakControl.notifier(), tests, ignored, threadLeakControl);
          s = withClassBefores(s);
          s = withClassAfters(s);
          s = withClassRules(s);
          s = withCloseContextResources(s, LifecycleScope.SUITE);
          s = threadLeakControl.forSuite(s, suiteDescription);
          try {
            s.evaluate();
          } catch (Throwable t) {
            t = augmentStackTrace(t, runnerRandomness);
            if (isAssumptionViolated(t)) {
              // Fire assumption failure before method ignores. (GH-103).
              notifier.fireTestAssumptionFailed(new Failure(suiteDescription, t));
  
              // Class level assumptions cause all tests to be ignored.
              // see Rants#RANT_3
              for (final TestCandidate c : tests) {
                notifier.fireTestIgnored(c.description);
              }
            } else {
              fireTestFailure(notifier, suiteDescription, t);
            }
          }
        }
      }
    } catch (Throwable t) {
      notifier.fireTestFailure(new Failure(suiteDescription, t));
    }

    // Fire a synthetic "suite ended" event and unsubscribe listeners.
    for (RunListener r : autoListeners) {
      try {
        r.testRunFinished(result);
      } catch (Throwable e) {
        logger.log(Level.SEVERE, "Panic: RunListener hook shouldn't throw exceptions.", e);
      }
    }

    // Final cleanup.
    notifier.removeListener(accounting);
    unsubscribeListeners(notifier);
    context.popAndDestroy();    
  }

  /**
   * Determine the set of ignored tests.
   */
  private Map<TestCandidate, Boolean> determineIgnoredTests(List<TestCandidate> tests) {
    Map<TestCandidate, Boolean> ignoredTests = new IdentityHashMap<>();
    for (TestCandidate c : tests) {
      // If it's an @Ignore-marked test, always report it as ignored, remove it from execution.
      if (hasIgnoreAnnotation(c) ) {
        ignoredTests.put(c, true);
      }

      // Otherwise, check if the test should be ignored due to test group annotations or filtering
      // expression
      if (isTestFiltered(groupEvaluator, c)) {
        // If we're running under an IDE, report the test back as ignored. Otherwise
        // check if filtering expression is being used. If not, report the test as ignored
        // (test group exclusion at work).
        if (containerRunner == RunnerContainer.ECLIPSE ||
            containerRunner == RunnerContainer.IDEA ||
            !groupEvaluator.hasFilteringExpression()) {
          ignoredTests.put(c, true);
        } else {
          ignoredTests.put(c, false);
        }
      }
    }
    return ignoredTests;
  }

  /**
   * Wrap with a rule to close context resources. 
   */
  private static Statement withCloseContextResources(final Statement s, final LifecycleScope scope) {
    return new StatementAdapter(s) {
      @Override
      protected void afterAlways(final List<Throwable> errors) throws Throwable {
        final ObjectProcedure<CloseableResourceInfo> disposer = new ObjectProcedure<CloseableResourceInfo>() {
          public void apply(CloseableResourceInfo info) {
            try {
              info.getResource().close();
            } catch (Throwable t) {
              ResourceDisposalError e = new ResourceDisposalError(
                  "Resource in scope " +
                  info.getScope().name() + " failed to close. Resource was" 
                      + " registered from thread " + info.getThreadName() 
                      + ", registration stack trace below.", t);
              e.setStackTrace(info.getAllocationStack());
              errors.add(e);
            }
          }
        };

        RandomizedContext.current().closeResources(disposer, scope);          
      }
    }; 
  }

  private Statement runTestsStatement(
      final RunNotifier notifier, 
      final List<TestCandidate> tests, 
      final Map<TestCandidate, Boolean> ignored, 
      final ThreadLeakControl threadLeakControl) {
    return new Statement() {
      public void evaluate() throws Throwable {
        for (final TestCandidate c : tests) {
          if (threadLeakControl.isTimedOut()) {
            break;
          }
          
          // Setup test thread's name so that stack dumps produce seed, test method, etc.
          final String testThreadName = "TEST-" + Classes.simpleName(suiteClass) +
              "." + c.method.getName() + "-seed#" + SeedUtils.formatSeedChain(runnerRandomness);
          final String restoreName = Thread.currentThread().getName();

          // This has a side effect of setting up a nested context for the test thread.
          final RandomizedContext current = RandomizedContext.current();
          try {
            Thread.currentThread().setName(testThreadName);
            current.push(new Randomness(c.seed, randomSupplier));
            current.setTargetMethod(c.method);
            
            if (ignored.containsKey(c)) {
              // Ignore the test, but report only if requested.
              if (ignored.get(c)) {
                reportAsIgnored(notifier, groupEvaluator, c);
              }
            } else {
              runSingleTest(notifier, c, threadLeakControl);
            }
          } finally {
            Thread.currentThread().setName(restoreName);
            current.setTargetMethod(null);
            current.popAndDestroy();
          }
        }              
      }
    };
  }

  void reportAsIgnored(RunNotifier notifier, GroupEvaluator ge, TestCandidate c) {
    if (c.method.getAnnotation(Ignore.class) != null) {
      notifier.fireTestIgnored(c.description);
      return;
    }

    String ignoreReason = ge.getIgnoreReason(c.method, suiteClass);
    if (ignoreReason != null) {
      notifier.fireTestStarted(c.description);
      notifier.fireTestAssumptionFailed(new Failure(c.description,
          new AssumptionViolatedException(ignoreReason)));
      notifier.fireTestFinished(c.description);
    }
  }

  private void fireTestFailure(RunNotifier notifier, Description description, Throwable t) {
    if (t instanceof MultipleFailureException) {
      for (Throwable nested : ((MultipleFailureException) t).getFailures()) {
        fireTestFailure(notifier, description, nested);
      }
    } else {
      notifier.fireTestFailure(new Failure(description, t));      
    }
  }

  /**
   * Decorate a {@link Statement} with {@link BeforeClass} hooks.
   */
  private Statement withClassBefores(final Statement s) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          for (Method method : getShuffledMethods(BeforeClass.class)) {
            invoke(method, null);
          }
        } catch (Throwable t) {
          throw augmentStackTrace(t, runnerRandomness);
        }
        s.evaluate();
      }
    };
  }

  private Statement withClassAfters(final Statement s) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        List<Throwable> errors = new ArrayList<Throwable>();
        try {
          s.evaluate();
        } catch (Throwable t) {
          errors.add(augmentStackTrace(t, runnerRandomness));
        }

        for (Method method : getShuffledMethods(AfterClass.class)) {
          try {
            invoke(method, null);
          } catch (Throwable t) {
            errors.add(augmentStackTrace(t, runnerRandomness));
          }
        }

        MultipleFailureException.assertEmpty(errors);
      }
    };
  }

  /**
   * Wrap with {@link ClassRule}s.
   */
  private Statement withClassRules(Statement s) {
    List<TestRule> classRules = getAnnotatedFieldValues(null, ClassRule.class, TestRule.class);
    for (TestRule rule : classRules) {
      s = rule.apply(s, suiteDescription);
    }
    return s;
  }

  /**
   * Runs a single test in the "master" test thread.
   */
  void runSingleTest(final RunNotifier notifier, 
                     final TestCandidate c,
                     final ThreadLeakControl threadLeakControl) {
    notifier.fireTestStarted(c.description);

    try {
      // Get the test instance.
      final Object instance = c.instanceProvider.newInstance();

      // Collect rules and execute wrapped method.
      Statement s = new Statement() {
        public void evaluate() throws Throwable {
          invoke(c.method, instance);
        }
      };

      s = wrapExpectedExceptions(s, c);
      s = wrapBeforeAndAfters(s, c, instance);
      s = wrapMethodRules(s, c, instance);
      s = withCloseContextResources(s, LifecycleScope.TEST);
      s = threadLeakControl.forTest(s, c);
      s.evaluate();
    } catch (Throwable e) {
      e = augmentStackTrace(e);
      if (isAssumptionViolated(e)) {
        notifier.fireTestAssumptionFailed(new Failure(c.description, e));
      } else {
        fireTestFailure(notifier, c.description, e);
      }
    } finally {
      notifier.fireTestFinished(c.description);
    }
  }

  /**
   * Wrap before and after hooks.
   */
  private Statement wrapBeforeAndAfters(Statement s, final TestCandidate c, final Object instance) {
    // Process @Before hooks. The first @Before to fail will immediately stop processing any other @Befores.
    final List<Method> befores = getShuffledMethods(Before.class);
    if (!befores.isEmpty()) {
      final Statement afterBefores = s;
      s = new Statement() {
        @Override
        public void evaluate() throws Throwable {
          for (Method m : befores) {
            invoke(m, instance);
          }
          afterBefores.evaluate();
        }
      };
    }

    // Process @After hooks. All @After hooks are processed, regardless of their own exceptions.
    final List<Method> afters = getShuffledMethods(After.class);
    if (!afters.isEmpty()) {
      final Statement beforeAfters = s;
      s = new Statement() {
        @Override
        public void evaluate() throws Throwable {
          List<Throwable> cumulative = new ArrayList<Throwable>();
          try {
            beforeAfters.evaluate();
          } catch (Throwable t) {
            cumulative.add(t);
          }

          // All @Afters must be called.
          for (Method m : afters) {
            try {
              invoke(m, instance);
            } catch (Throwable t) {
              cumulative.add(t);
            }
          }

          // At end, throw the exception or propagete.
          if (cumulative.size() == 1) {
            throw cumulative.get(0);
          } else if (cumulative.size() > 1) {
            throw new MultipleFailureException(cumulative);
          }
        }
      };
    }

    return s;
  }

  /**
   * Wrap the given statement into another catching the expected exception, if declared.
   */
  private Statement wrapExpectedExceptions(final Statement s, TestCandidate c) {
    Test ann = c.method.getAnnotation(Test.class);

    if (ann == null) {
      return s;
    }
    
    // If there's no expected class, don't wrap. Eh, None is package-private...
    final Class<? extends Throwable> expectedClass = ann.expected();
    if (expectedClass.getName().equals("org.junit.Test$None")) {
      return s;
    }

    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          s.evaluate();
        } catch (Throwable t) {
          if (!expectedClass.isInstance(t)) {
            throw t;
          }
          // We caught something that was expected. No worries then.
          return;
        }
        
        // If we're here this means we passed the test that expected a failure.
        Assert.fail("Expected an exception but the test passed: "
            + expectedClass.getName());
      }
    };
  }

  /**
   * Wrap the given statement in any declared MethodRules (old style rules).
   */
  @SuppressWarnings("deprecation")
  private Statement wrapMethodRules(Statement s, TestCandidate c, Object instance) {
    FrameworkMethod fm = new FrameworkMethod(c.method);

    // Old-style MethodRules first.
    List<org.junit.rules.MethodRule> methodRules = 
        getAnnotatedFieldValues(instance, Rule.class, org.junit.rules.MethodRule.class);
    for (org.junit.rules.MethodRule rule : methodRules) {
      s = rule.apply(s, fm, instance);
    }

    // New-style TestRule next.
    List<TestRule> testRules = 
        getAnnotatedFieldValues(instance, Rule.class, TestRule.class);
    for (TestRule rule : testRules) {
      s = rule.apply(s, c.description);
    }

    return s;
  }

  /*
   * We're using JUnit infrastructure here, but provide constant 
   * ordering of the result. The returned list has class...super order.
   */
  private <T> List<T> getAnnotatedFieldValues(Object test,
      Class<? extends Annotation> annotationClass, Class<T> valueClass) {
    TestClass info = AccessController.doPrivileged(new PrivilegedAction<TestClass>() {
      @Override
      public TestClass run() {
        return new TestClass(suiteClass);
      }
    });
    List<T> results = new ArrayList<T>();

    List<FrameworkField> annotatedFields = 
        new ArrayList<FrameworkField>(info.getAnnotatedFields(annotationClass));

    // Split fields by class
    final HashMap<Class<?>, List<FrameworkField>> byClass = 
        new HashMap<Class<?>, List<FrameworkField>>();
    for (FrameworkField field : annotatedFields) {
      Class<?> clz = field.getField().getDeclaringClass();
      if (!byClass.containsKey(clz)) {
        byClass.put(clz, new ArrayList<FrameworkField>());
      }
      byClass.get(clz).add(field);
    }

    // Consistent order at class level.
    for (List<FrameworkField> fields : byClass.values()) {
      Collections.sort(fields, new Comparator<FrameworkField>() {
        @Override
        public int compare(FrameworkField o1, FrameworkField o2) {
          return o1.getField().getName().compareTo(
                 o2.getField().getName());
        }
      });
      Collections.shuffle(fields, new Random(runnerRandomness.getSeed()));
    }

    annotatedFields.clear();
    for (Class<?> clz = suiteClass; clz != null; clz = clz.getSuperclass()) {
      List<FrameworkField> clzFields = byClass.get(clz);
      if (clzFields != null) {
        annotatedFields.addAll(clzFields);
      }
    }

    for (FrameworkField each : annotatedFields) {
      try {
        Object fieldValue = each.get(test);
        if (valueClass.isInstance(fieldValue))
          results.add(valueClass.cast(fieldValue));
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    return results;
  }

  /**
   * Create randomized context for the run. The context is shared by all
   * threads in a given thread group (but the source of {@link Randomness} 
   * is assigned per-thread).
   */
  private RandomizedContext createContext(ThreadGroup tg) {
    return RandomizedContext.create(tg, suiteClass, this);
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

  private static List<TestCandidate> applyFilters(Class<?> suiteClass, 
                                                  List<TestCandidate> testCandidates, 
                                                  Collection<Filter> testFilters) {
    final List<TestCandidate> filtered;
    if (testFilters.isEmpty()) {
      filtered = new ArrayList<TestCandidate>(testCandidates);
    } else {
      filtered = new ArrayList<>(testCandidates.size());
      for (TestCandidate candidate : testCandidates) {
        boolean shouldRun = true;
        for (Filter f : testFilters) {
          // Inquire for both full description (possibly with parameters and seed)
          // and simplified description (just method name).
          if (f.shouldRun(candidate.description) ||
              f.shouldRun(Description.createTestDescription(
                  suiteClass, candidate.method.getName()))) {
            continue;
          }

          shouldRun = false;
          break;
        }

        if (shouldRun) {
          filtered.add(candidate);
        }
      }
    }
    return filtered;
  }

  /**
   * Normalize empty strings to nulls.
   */
  static String emptyToNull(String value) {
    if (value == null || value.trim().isEmpty())
      return null;
    return value.trim();
  }

  /** 
   * Returns true if we should ignore this test candidate.
   */
  private boolean hasIgnoreAnnotation(TestCandidate c) {
    return c.method.getAnnotation(Ignore.class) != null;
  }

  private boolean isTestFiltered(GroupEvaluator ev, TestCandidate c) {
    return ev.getIgnoreReason(c.method, suiteClass) != null;
  }

  /**
   * Construct a list of ordered framework methods. Minor tweaks are done depending
   * on the annotation (reversing order, etc.). 
   */
  private List<Method> getShuffledMethods(Class<? extends Annotation> ann) {
    List<Method> methods = shuffledMethodsCache.get(ann);
    if (methods != null) {
      return methods;
    }

    methods = new ArrayList<Method>(classModel.getAnnotatedLeafMethods(ann).keySet());

    // Shuffle sub-ranges using class level randomness.
    Random rnd = new Random(runnerRandomness.getSeed());
    for (int i = 0, j = 0; i < methods.size(); i = j) {
      final Method m = methods.get(i);
      j = i + 1;
      while (j < methods.size() && m.getDeclaringClass() == methods.get(j).getDeclaringClass()) {
        j++;
      }

      if (j - i > 1) {
        Collections.shuffle(methods.subList(i, j), rnd);
      }
    }

    // Reverse processing order to super...clazz for befores
    if (ann == Before.class || ann == BeforeClass.class) {
      Collections.reverse(methods);
    }

    methods = Collections.unmodifiableList(methods);
    shuffledMethodsCache.put(ann, methods);
    return methods;
  }

  /**
   * Collect all test candidates, regardless if they will be executed or not. At this point
   * individual test methods are also expanded into multiple executions corresponding
   * to the number of iterations ({@link SysGlobals#SYSPROP_ITERATIONS}) and the initial method seed
   * is preassigned. 
   * 
   * <p>The order of test candidates is shuffled based on the runner's random.</p> 
   * 
   * @see Rants#RANT_1
   */
  private List<TestCandidate> collectTestCandidates(Description classDescription) {
    // Get the test instance provider if explicitly stated.
    TestMethodProviders providersAnnotation = 
        suiteClass.getAnnotation(TestMethodProviders.class);

    // If nothing, fallback to the default.
    final TestMethodProvider [] providers;
    if (providersAnnotation != null) {
      providers = new TestMethodProvider [providersAnnotation.value().length];
      int i = 0;
      for (Class<? extends TestMethodProvider> clazz : providersAnnotation.value()) {
        try {
          providers[i++] = clazz.newInstance();
        } catch (Exception e) {
          throw new RuntimeException(TestMethodProviders.class.getSimpleName() +
          		" classes could not be instantiated.", e);
        }
      }
    } else {
      providers = new TestMethodProvider [] {
          new JUnit4MethodProvider(),
          // new JUnit3MethodProvider(),
      };
    }

    // Get test methods from providers.
    final Set<Method> allTestMethods = new HashSet<Method>();
    for (TestMethodProvider provider : providers) {
      Collection<Method> testMethods = provider.getTestMethods(suiteClass, classModel);
      allTestMethods.addAll(testMethods);
    }

    List<Method> testMethods = new ArrayList<Method>(allTestMethods);
    Collections.sort(testMethods, new Comparator<Method>() {
      @Override
      public int compare(Method m1, Method m2) {
        return m1.toGenericString().compareTo(m2.toGenericString());
      }
    });

    // Perform candidate method validation.
    validateTestMethods(testMethods);

    // Random (but consistent) shuffle.
    Collections.shuffle(testMethods, new Random(runnerRandomness.getSeed()));

    Constructor<?>[] constructors = suiteClass.getConstructors();
    if (constructors.length != 1) {
      throw new RuntimeException("There must be exactly one constructor: " + constructors.length);
    }
    final Constructor<?> constructor = constructors[0];

    // Collect test method-parameters pairs.
    List<TestMethodExecution> testCases = collectMethodExecutions(constructor, testMethods);

    // Test case ordering. Shuffle only real test cases, don't allow shuffling
    // or changing the order of reiterations or explicit @Seed annotations that
    // multiply a given test.
    TestCaseOrdering methodOrder = suiteClass.getAnnotation(TestCaseOrdering.class);
    if (methodOrder != null) {
      try {
        Collections.sort(testCases, methodOrder.value().newInstance());
      } catch (InstantiationException | IllegalAccessException e) {
        throw new RuntimeException("Could not sort test methods.", e);
      }
    }

    // Collect all variants of execution for a single method/ parameters pair.
    Map<String, Integer> descriptionRepetitions = new HashMap<>();
    List<TestCandidate> allTests = new ArrayList<TestCandidate>();
    Map<Method, List<TestCandidate>> sameMethodVariants = new LinkedHashMap<Method, List<TestCandidate>>();
    for (TestMethodExecution testCase : testCases) {
      List<TestCandidate> variants = collectCandidatesForMethod(descriptionRepetitions, constructor, testCase);
      allTests.addAll(variants);

      List<TestCandidate> existing = sameMethodVariants.get(testCase.method);
      if (existing == null) {
        existing = new ArrayList<>(variants);
        sameMethodVariants.put(testCase.method, existing);
      } else {
        existing.addAll(variants);
      }
    }

    // Rearrange JUnit Description into a hierarchy if a given method
    // has more than one variant (due to multiple repetitions, parameters or seeds).
    for (Map.Entry<Method, List<TestCandidate>> e : sameMethodVariants.entrySet()) {
      List<TestCandidate> candidates = e.getValue();
      if (candidates.size() > 1) {
        Description methodParent = Description.createSuiteDescription(e.getKey().getName());
        suiteDescription.addChild(methodParent);
        for (TestCandidate candidate : candidates) {
          methodParent.addChild(candidate.description);
        }
      } else {
        suiteDescription.addChild(candidates.iterator().next().description);
      }
    }

    return allTests;
  }

  /**
   * Helper tuple (Method, instance params).
   */
  static private class TestMethodExecution implements TestMethodAndParams {
    final Object [] params;
    final List<Object> paramsWrapper;
    final Method method;
    final String argFormattingTemplate;
    final InstanceProvider instanceProvider;

    public TestMethodExecution(Method m, String argFormattingTemplate, Object[] params, InstanceProvider instanceProvider) {
      this.method = m;
      this.params = params;
      this.paramsWrapper = Collections.unmodifiableList(Arrays.asList(params));
      this.argFormattingTemplate = argFormattingTemplate;
      this.instanceProvider = instanceProvider;
    }

    @Override
    public Method getTestMethod() {
      return method;
    }

    @Override
    public List<Object> getInstanceArguments() {
      return paramsWrapper;
    }
  }
  
  /**
   * Collect test candidates for a single method and the given seed.
   */
  private List<TestCandidate> collectCandidatesForMethod(
      Map<String,Integer> descriptionRepetitions, 
      final Constructor<?> constructor, 
      TestMethodExecution testCase) {
    final Method method = testCase.method;
    final Object[] params = testCase.params;
    final boolean fixedSeed = isConstantSeedForAllIterations(method);
    final int methodIterations = determineMethodIterationCount(method);
    final long[] seeds = determineMethodSeeds(method);

    final List<TestCandidate> candidates = new ArrayList<TestCandidate>();

    String argFormattingTemplate = testCase.argFormattingTemplate;
    if (methodIterations > 1 || seeds.length > 1 || appendSeedParameter) {
      final int seedParamIndex = params.length + 1;
      argFormattingTemplate += " seed=%" + seedParamIndex + "$s";
    }

    for (final long testSeed : seeds) {
      for (int i = 0; i < methodIterations; i++) {
        final long thisSeed = (fixedSeed ? testSeed : testSeed ^ MurmurHash3.hash((long) i));

        // Format constructor arguments.
        Object [] args = Arrays.copyOf(testCase.params, testCase.params.length + 1, Object[].class);
        args[args.length - 1] = SeedUtils.formatSeedChain(runnerRandomness, new Randomness(thisSeed, randomSupplier));
        String formattedArguments = String.format(Locale.ROOT, argFormattingTemplate, args);

        String key = method.getName() + "::" + formattedArguments;
        int cnt = 1 + zeroForNull(descriptionRepetitions.get(key));
        descriptionRepetitions.put(key, cnt);
        if (cnt > 1) {
          formattedArguments += " #" + cnt;
        }

        if (!formattedArguments.trim().isEmpty()) {
          // GH-253: IntelliJ only recognizes test names for re-runs when " [...]" is used...
          // Leave for now (backward compat?)
          if (containerRunner == RunnerContainer.IDEA) {
            formattedArguments = " [" + formattedArguments.trim() + "]";
          } else {
            formattedArguments = " {" + formattedArguments.trim() + "}";
          }
        }

        Description description = Description.createSuiteDescription(
            String.format(Locale.ROOT, "%s%s(%s)", method.getName(), formattedArguments, suiteClass.getName()),
            method.getAnnotations());

        // Create an instance and delay instantiation exception if possible.
        candidates.add(new TestCandidate(method, thisSeed, description, testCase.instanceProvider));
      }
    }

    return candidates;
  }

  /** Replace null with zero. */
  private static int zeroForNull(Integer v) {
    return v == null ? 0 : v;
  }

  /**
   * Collect test method executions from list of test methods and
   * potentially parameters from parameter factory methods. 
   */
  public List<TestMethodExecution> collectMethodExecutions(Constructor<?> constructor, List<Method> testMethods) {
    final List<TestMethodExecution> testCases = new ArrayList<>();
    String argFormattingTemplate = createDefaultArgumentFormatting(constructor);
    final Map<Method, MethodModel> factoryMethods = classModel.getAnnotatedLeafMethods(ParametersFactory.class);

    if (factoryMethods.isEmpty()) {
      Object[] noArgs = new Object [0];
      InstanceProvider instanceProvider = getInstanceProvider(constructor, noArgs);
      for (Method testMethod : testMethods) {
        testCases.add(new TestMethodExecution(testMethod, argFormattingTemplate, noArgs, instanceProvider));
      }
    } else {
      for (Method factoryMethod : factoryMethods.keySet()) {
        Validation.checkThat(factoryMethod)
          .isStatic()
          .isPublic();

        if (!Iterable.class.isAssignableFrom(factoryMethod.getReturnType())) {
          throw new RuntimeException("@" + ParametersFactory.class.getSimpleName() + " annotated " +
              "methods must be public, static and returning Iterable<Object[]>:" + factoryMethod);
        }

        ParametersFactory pfAnnotation = factoryMethod.getAnnotation(ParametersFactory.class);
        if (!pfAnnotation.argumentFormatting().equals(ParametersFactory.DEFAULT_FORMATTING)) {
          argFormattingTemplate = pfAnnotation.argumentFormatting();
        }

        List<Object[]> args = new ArrayList<>();
        try {
          Iterable<?> factoryArguments = Iterable.class.cast(factoryMethod.invoke(null));
          for (Object o : factoryArguments) {
            if (!(o instanceof Object[])) {
              throw new RuntimeException("Expected Object[] for each set of constructor arguments: " + o);
            }
            args.add((Object[]) o);
          }
        } catch (InvocationTargetException e) {
          if (isAssumptionViolated(e.getCause())) {
            return Collections.emptyList();
          }
          Rethrow.rethrow(e.getCause());
        } catch (Throwable t) {
          throw new RuntimeException("Error collecting parameters from: " + factoryMethod, t);
        }

        if (pfAnnotation.shuffle()) {
          Collections.shuffle(args, new Random(runnerRandomness.getSeed()));
        }

        for (Object[] constructorArgs : args) {
          InstanceProvider instanceProvider = getInstanceProvider(constructor, constructorArgs);
          for (Method testMethod : testMethods) {
            testCases.add(new TestMethodExecution(testMethod, argFormattingTemplate, constructorArgs, instanceProvider));
          }
        }
      }
    }
    return testCases;  
  }

  private boolean isAssumptionViolated(Throwable cause) {
    return cause instanceof org.junit.AssumptionViolatedException ||
           cause instanceof org.junit.internal.AssumptionViolatedException;
  }

  /**
   * Determine instance provider. 
   */
  private InstanceProvider getInstanceProvider(Constructor<?> constructor, Object[] args) {
    TestCaseInstanceProvider.Type type = TestCaseInstanceProvider.Type.INSTANCE_PER_TEST_METHOD; 
    TestCaseInstanceProvider providerAnn = suiteClass.getAnnotation(TestCaseInstanceProvider.class);
    if (providerAnn != null) {
      type = providerAnn.value();
    }

    switch (type) {
      case INSTANCE_PER_CONSTRUCTOR_ARGS:
        return new SameInstanceProvider(new NewInstanceProvider(constructor, args));
      case INSTANCE_PER_TEST_METHOD:
        return new NewInstanceProvider(constructor, args);
      default:
        throw new RuntimeException();
    }
  }
  
  private static class SameInstanceProvider implements InstanceProvider {
    private final InstanceProvider delegate;
    private volatile Object instance;

    public SameInstanceProvider(InstanceProvider delegate) {
      this.delegate = delegate;
    }

    @Override
    public Object newInstance() throws Throwable {
      // There should be no concurrent-threaded access to this method, ever,
      // but we can be called from multiple threads sequentially.
      if (instance == null) {
        instance = delegate.newInstance();
      }
      return instance;
    }
  }

  private static class NewInstanceProvider implements InstanceProvider {
    private final Constructor<?> constructor;
    private final Object[] args;

    public NewInstanceProvider(Constructor<?> constructor, Object[] args) {
      this.constructor = constructor;
      this.args = args;
    }

    @Override
    public Object newInstance() throws Throwable {
      try {
        return constructor.newInstance(args);
      } catch (InvocationTargetException e) {
        throw ((InvocationTargetException) e).getTargetException();
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            "Constructor arguments do not match provider parameters?", e);
      }
    }
  }

  /**
   * Default formatting string for constructor arguments.
   */
  private static String createDefaultArgumentFormatting(Constructor<?> constructor) {
    StringBuilder b = new StringBuilder();
    final int argCount = constructor.getParameterTypes().length;
    Annotation [][] anns = constructor.getParameterAnnotations();
    for (int i = 0; i < argCount; i++) {
      String argName = null;

      for (Annotation ann : anns[i]) {
        if (ann != null && ann.annotationType().equals(Name.class)) {
          argName = ((Name) ann).value();
          break;
        }
      }

      if (argName == null) {
        argName = "p" + i;
      }

      b.append(i > 0 ? " " : "")
       .append(argName)
       .append("=%s");
    }

    return b.toString();
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
   *  <li>global property {@link SysGlobals#SYSPROP_ITERATIONS}.</li>
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
      return new long [] { testCaseRandomnessOverride.getSeed() };
    }

    // We assign each method a different starting hash based on the global seed
    // and a hash of their name (so that the order of methods does not matter, only
    // their names). Take into account global override and method and class level
    // {@link Seed} annotations.    
    final long randomSeed = 
        runnerRandomness.getSeed() ^ MurmurHash3.hash((long) method.getName().hashCode());
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
    Seeds seedsValue = classModel.getAnnotation(method, Seeds.class, true);
    if (seedsValue != null) {
      for (Seed s : seedsValue.value()) {
        if (s.value().equals("random"))
          seeds.add(randomSeed);
        else {
          for (long s2 : SeedUtils.parseSeedChain(s.value())) {
            seeds.add(s2);
          }
        }
      }
    }

    // Check suite-level override.
    if (seeds.isEmpty()) {
      if ((seed = suiteClass.getAnnotation(Seed.class)) != null) {
        if (!seed.value().equals("random")) {
          long [] seedChain = SeedUtils.parseSeedChain(suiteClass.getAnnotation(Seed.class).value());
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
   * Invoke a given method on a suiteClass instance (can be null for static methods).
   */
  void invoke(final Method m, Object instance, Object... args) throws Throwable {
    if (!Modifier.isPublic(m.getModifiers())) {
      try {
        if (!m.isAccessible()) {
          AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
              m.setAccessible(true);
              return null;
            }});
        }
      } catch (SecurityException e) {
        throw new RuntimeException("There is a non-public method that needs to be called. This requires " +
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
   * Perform additional checks on methods returned from the providers. 
   */
  private void validateTestMethods(List<Method> testMethods) {
    HashSet<Class<?>> parents = new HashSet<Class<?>>();
    for (Class<?> c = suiteClass; c != null; c = c.getSuperclass()) {
      parents.add(c);
    }

    for (Method method : testMethods) {
      if (!parents.contains(method.getDeclaringClass())) {
        throw new IllegalArgumentException("Test method does not belong to " +
        		"test suite class hierarchy: " + method.getDeclaringClass() + "#" +
            method.getName());
      }

      // * method()
      Validation.checkThat(method)
        .describedAs("Test method " + suiteClass.getName() + "#" + method.getName())
        .isPublic()
        .isNotStatic()
        .hasArgsCount(0);

      // No @Test(timeout=...) and @Timeout at the same time.
      Test testAnn = classModel.getAnnotation(method, Test.class, true);
      if (testAnn != null && testAnn.timeout() > 0 && classModel.isAnnotationPresent(method, Timeout.class, true)) {
        throw new IllegalArgumentException("Conflicting @Test(timeout=...) and @Timeout " +
            "annotations in: " + suiteClass.getName() + "#" + method.getName());
      }

      // @Seed annotation on test methods must have at most 1 seed value.
      Seed seed = classModel.getAnnotation(method, Seed.class, true);
      if (seed != null) {
        try {
          String seedChain = seed.value();
          if (!seedChain.equals("random")) {
            long[] chain = SeedUtils.parseSeedChain(seedChain);
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
      .isConcreteClass();

    // Check constructors.
    Constructor<?> [] constructors = suiteClass.getConstructors();
    if (constructors.length != 1 || !Modifier.isPublic(constructors[0].getModifiers())) {
      throw new RuntimeException("A test class is expected to have one public constructor "
          + " (parameterless or with types matching static @" + ParametersFactory.class 
          + "-annotated method's output): " + suiteClass.getName());
    }

    // If there is a parameterized constructor, look for a static method that privides parameters.
    if (constructors[0].getParameterTypes().length > 0) {
      Collection<Method> factories = classModel.getAnnotatedLeafMethods(ParametersFactory.class).keySet();
      if (factories.isEmpty()) {
        throw new RuntimeException("A test class with a parameterized constructor is expected "
            + " to have a static @" + ParametersFactory.class 
            + "-annotated method: " + suiteClass.getName());
      }

      for (Method m : factories) {
        Validation.checkThat(m)
          .describedAs("@ParametersFactory method " + suiteClass.getName() + "#" + m.getName())
          .isStatic()
          .isPublic()
          .hasArgsCount(0)
          .hasReturnType(Iterable.class);
      }
    }

    // @BeforeClass
    for (Method method : classModel.getAnnotatedLeafMethods(BeforeClass.class).keySet()) {
      Validation.checkThat(method)
        .describedAs("@BeforeClass method " + suiteClass.getName() + "#" + method.getName())
        .isStatic()
        .hasArgsCount(0);
    }

    // @AfterClass
    for (Method method : classModel.getAnnotatedLeafMethods(AfterClass.class).keySet()) {
      Validation.checkThat(method)
        .describedAs("@AfterClass method " + suiteClass.getName() + "#" + method.getName())
        .isStatic()
        .hasArgsCount(0);
    }

    // @Before
    for (Method method : classModel.getAnnotatedLeafMethods(Before.class).keySet()) {
      Validation.checkThat(method)
        .describedAs("@Before method " + suiteClass.getName() + "#" + method.getName())
        .isNotStatic()
        .hasArgsCount(0);
    }

    // @After
    for (Method method : classModel.getAnnotatedLeafMethods(After.class).keySet()) {
      Validation.checkThat(method)
        .describedAs("@After method " + suiteClass.getName() + "#" + method.getName())
        .isNotStatic()
        .hasArgsCount(0);
    }

    // TODO: [GH-214] Validate @Rule fields (what are the "rules" for these anyway?)
  }

  /**
   * Augment stack trace of the given exception with seed infos.
   */
  static <T extends Throwable> T augmentStackTrace(T e, Randomness... seeds) {
    if (seeds.length == 0) {
      seeds = RandomizedContext.current().getRandomnesses();
    }

    final String seedChain = SeedUtils.formatSeedChain(seeds);
    final String existingSeed = seedFromThrowable(e);  
    if (existingSeed != null && existingSeed.equals(seedChain)) {
      return e;
    }

    List<StackTraceElement> stack = new ArrayList<StackTraceElement>(
        Arrays.asList(e.getStackTrace()));
  
    stack.add(0,  new StackTraceElement(AUGMENTED_SEED_PACKAGE + ".SeedInfo", 
        "seed", seedChain, 0));

    e.setStackTrace(stack.toArray(new StackTraceElement [stack.size()]));
    return e;
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
  
    return SeedUtils.parseSeedChain(seedChain);
  }

  /**
   * Stack trace formatting utilities. These may be initialized to filter out certain packages.  
   */
  public TraceFormatting getTraceFormatting() {
    return traces;
  }

  /**
   * {@link RandomizedRunner} augments stack traces of test methods that ended in an exception
   * and inserts a fake entry starting with {@link #AUGMENTED_SEED_PACKAGE}.
   * 
   * @return A string is returned with seeds combined, if any. Null is returned if no augmentation
   * can be found. 
   */
  public static String seedFromThrowable(Throwable t) {
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
   * Attempts to extract just the method name from parameterized notation. 
   */
  public static String methodName(Description description) {
    return description.getMethodName().replaceAll("\\s?\\{.+\\}", "");
  }

  /**
   * Returns true if any previous (or current) suite marked with 
   * {@link Consequence#IGNORE_REMAINING_TESTS} has
   * left zombie threads.
   */
  public static boolean hasZombieThreads() {
    return zombieMarker.get();
  }
}
