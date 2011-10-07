package com.carrotsearch.randomizedtesting;


import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.MethodRule;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import static com.carrotsearch.randomizedtesting.Randomness.*;

// TODO: what about TestRule support (introduced in 4.9?). If we rely on it explicitly, will this cause problems with older shells (ant, eclipse)?

/**
 * A somewhat less hairy (?), no-fancy {@link Runner} implementation for 
 * running randomized tests.
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
 *   methods declared within the same class are called in <b>randomized</b> order,</li>
 *   <li>
 * </ul>
 * 
 * <p>Deviations from "standard" JUnit:
 * <ul>
 *   <li>test methods are allowed to return values (the return value is ignored),</li>
 *   <li>all exceptions are reported to the notifier, there is no suppression or 
 *   chaining of exceptions,</li>
 * </ul>
 */
@SuppressWarnings("deprecation")
public final class RandomizedRunner extends Runner implements Filterable {
  /**
   * System property with an integer defining global initialization seeds for all
   * random generators. Should guarantee test reproducibility.
   */
  public static final String SYSPROP_RANDOM_SEED = "randomized.seed";

  /**
   * The global override for the number of each test's repetitions.
   */
  public static final String SYSPROP_ITERATIONS = "randomized.iters";

  /**
   * Test candidate (model).
   */
  private static class TestCandidate {
    public final Randomness randomness;
    public final Description description;
    public final FrameworkMethod method;

    public TestCandidate(FrameworkMethod method, Randomness rnd, Description description) {
      this.randomness = rnd;
      this.description = description;
      this.method = method;
    }
  }
  
  /** The target class with test methods. */
  private final Class<?> target;

  /** JUnit utilities for scanning methods/ fields. */
  private final TestClass targetInfo;

  /** The runner's seed (master). */
  private final Randomness runnerRandomness;

  /** Override per-test case random seed from command line. */
  private Randomness testRandomnessOverride;

  /** The number of each test's randomized iterations (global). */
  private int iterations;

  /** All test candidates, flattened. */
  private List<TestCandidate> testCandidates;

  /** Class suite description. */
  private Description classDescription;

  /** Apply a user-level filter. */
  private Filter filter;

  /** Creates a new runner for the given class. */
  public RandomizedRunner(Class<?> testClass) throws InitializationError {
    this.target = testClass;
    this.targetInfo = new TestClass(testClass);

    // Fail fast if target is inconsistent.
    validateTarget();

    // Initialize the runner's master seed/ randomness source.
    final String globalSeed = System.getProperty(SYSPROP_RANDOM_SEED);
    if (globalSeed != null) {
      final long[] seedChain = parseSeedChain(globalSeed);
      if (seedChain.length == 0 || seedChain.length > 2) {
        throw new IllegalArgumentException("Invalid " 
            + SYSPROP_RANDOM_SEED + " specification: " + globalSeed);
      }

      if (seedChain.length > 1)
        testRandomnessOverride = new Randomness(seedChain[1]);
      runnerRandomness = new Randomness(seedChain[0]);
    } else if (target.getAnnotation(Seed.class) != null) {
      runnerRandomness = new Randomness(parseSeedChain(target.getAnnotation(Seed.class).value())[0]);
    } else {
      runnerRandomness = new Randomness(MurmurHash3.hash(System.currentTimeMillis()));
    }

    if (System.getProperty(SYSPROP_ITERATIONS) != null) {
      this.iterations = Integer.parseInt(System.getProperty(SYSPROP_ITERATIONS, "1"));
      if (iterations < 1)
        throw new IllegalArgumentException(SYSPROP_ITERATIONS + " must be >= 1: " + iterations);
    }

    // Collect all test candidates, regardless if they'll be executed or not.
    classDescription = Description.createSuiteDescription(target);
    testCandidates = collectTestCandidates(classDescription);
  }

  /**
   * Return the current tree of test descriptions (filtered).
   */
  @Override
  public Description getDescription() {
    return classDescription;
  }

  /**
   * Runs all tests and hooks.
   */
  @Override
  public void run(RunNotifier notifier) {
    RandomizedContext context = new RandomizedContext();
    context.targetClass = target;
    context.randomness = runnerRandomness;
    RandomizedContext.setContext(context);
    try {
      try {
        runBeforeClassMethods();
  
        for (TestCandidate c : testCandidates) {
          if (filter == null || filter.shouldRun(c.description)) {
            context.randomness = c.randomness;
            run(notifier, c);
          }
        }
      } catch (Throwable t) {
        notifier.fireTestFailure(new Failure(classDescription, t));
      }

      context.randomness = runnerRandomness;
      runAfterClassMethods(notifier);
    } finally {
      RandomizedContext.clearContext();
    }
  }

  /**
   * Runs a single test.
   */
  private void run(RunNotifier notifier, final TestCandidate c) {
    notifier.fireTestStarted(c.description);
    
    if (c.method.getAnnotation(Ignore.class) != null) {
      notifier.fireTestIgnored(c.description);
    } else {
      Object instance = null;
      try {
        // Get the test instance.
        instance = target.newInstance();

        // Run @Before hooks.
        for (FrameworkMethod m : getTargetMethods(Before.class))
          m.invokeExplosively(instance);
  
        // Collect rules and execute wrapped method.
        final Object finalizedInstance = instance;
        Statement s = new Statement() {
          public void evaluate() throws Throwable {
            c.method.invokeExplosively(finalizedInstance);
          }
        };
        for (MethodRule each : targetInfo.getAnnotatedFieldValues(target, Rule.class, MethodRule.class))
          s = each.apply(s, c.method, instance);
        s.evaluate();
      } catch (AssumptionViolatedException e) {
        notifier.fireTestAssumptionFailed(new Failure(c.description, e));
      } catch (Throwable e) {
        notifier.fireTestFailure(new Failure(c.description, e));
      }
  
      // Run @After hooks if an instance has been created.
      if (instance != null) {
        for (FrameworkMethod m : getTargetMethods(After.class)) {
          try {
            m.invokeExplosively(instance);
          } catch (Throwable t) {
            notifier.fireTestFailure(new Failure(c.description, t));
          }
        }
      }
    }

    notifier.fireTestFinished(c.description);
  }

  /**
   * Run before class methods. These fail immediately.
   */
  private void runBeforeClassMethods() throws Throwable {
    for (FrameworkMethod method : getTargetMethods(BeforeClass.class)) {
      method.invokeExplosively(null);
    }
  }

  /**
   * Run after class methods. Collect exceptions, execute all.
   */
  private void runAfterClassMethods(RunNotifier notifier) {
    for (FrameworkMethod method : getTargetMethods(AfterClass.class)) {
      try {
        method.invokeExplosively(null);
      } catch (Throwable t) {
        notifier.fireTestFailure(new Failure(classDescription, t));
      }
    }
  }

  /**
   * Implement {@link Filterable} because GUIs depend on it to run tests selectively.
   */
  @Override
  public void filter(Filter filter) throws NoTestsRemainException {
    this.filter = filter;
  }
  
  /**
   * Collect all test candidates, regardless if they'll be executed or not. At this point
   * individual test methods are also expanded into multiple executions corresponding
   * to the number of iterations ({@link #SYSPROP_ITERATIONS}) and the initial method seed 
   * is preassigned. 
   * 
   * @see Rants#RANT_1
   */
  private List<TestCandidate> collectTestCandidates(Description classDescription) {
    List<FrameworkMethod> testMethods = getTargetMethods(Test.class);
    List<TestCandidate> candidates = new ArrayList<TestCandidate>();
    for (FrameworkMethod method : testMethods) {
      Description parent = classDescription;
      int methodIterations = determineMethodIterationCount(method);
      if (methodIterations > 1) {
        // This will be un-clickable in Eclipse. See Rants.
        parent = Description.createSuiteDescription(method.getName());
        classDescription.addChild(parent);
      }

      final long testSeed = determineMethodSeed(method);
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
                "(" + target.getName() + ")");

        // Add the candidate.
        parent.addChild(description);
        candidates.add(new TestCandidate(method, iterRandomness, description));
      }
    }
    return candidates;
  }

  /**
   * Determine a given method's initial random seed.
   * We assign each method a different starting hash based on the global seed
   * and a hash of their name (so that the order of methods does not matter, only
   * their names). Take into account global override and method and class level
   * {@link Seed} annotations.
   * 
   * @see Seed
   */
  private long determineMethodSeed(FrameworkMethod method) {
    if (testRandomnessOverride != null) {
      return testRandomnessOverride.seed;
    }

    Seed seed;
    if ((seed = method.getAnnotation(Seed.class)) != null) {
      return parseSeedChain(seed.value())[0];
    }
    if ((seed = target.getAnnotation(Seed.class)) != null) {
      long [] seeds = parseSeedChain(target.getAnnotation(Seed.class).value());
      if (seeds.length > 1)
        return seeds[1];
    }
    return runnerRandomness.seed ^ method.getName().hashCode();
  }

  /**
   * Determine if a given method's iterations should run with a fixed seed or not.
   */
  private boolean isConstantSeedForAllIterations(FrameworkMethod method) {
    if (testRandomnessOverride != null)
      return true;

    Repeat repeat;
    if ((repeat = method.getAnnotation(Repeat.class)) != null) {
      return repeat.useConstantSeed();
    }
    if ((repeat = target.getAnnotation(Repeat.class)) != null) {
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
  private int determineMethodIterationCount(FrameworkMethod method) {
    // Global override.
    if (iterations > 0)
      return iterations;

    Repeat repeat;
    if ((repeat = method.getAnnotation(Repeat.class)) != null) {
      return repeat.iterations();
    }
    if ((repeat = target.getAnnotation(Repeat.class)) != null) {
      return repeat.iterations();
    }

    return /* default */ 1;
  }

  /**
   * Validate methods and hooks in the target.
   */
  private void validateTarget() {
    // Validate test methods.
    for (FrameworkMethod method : getTargetMethods(Test.class)) {
      Validation.checkThat(method.getMethod())
        .describedAs("Test method " + target.getName() + "#" + method.getName())
        .isPublic()
        .hasArgsCount(0);
    }

    // TODO: Validate target is accessible (public, conrete, has a parameterless constructor etc).
    // TODO: Validate @BeforeClass hooks.
    // TODO: Validate @AfterClass hooks.
    // TODO: Validate @Before hooks.
    // TODO: Validate @After hooks.
    // TODO: Validate @Rule fields.
    // TODO: Validate @Seed annotation on methods: must have at most 1 seed value.
  }

  /**
   * Returns a list of target methods with the given annotation. Includes the
   * logic of ordering the list properly for {@link BeforeClass}, {@link Before},
   * {@link After} and {@link AfterClass} methods. Shuffles methods at the same level.
   */
  private List<FrameworkMethod> getTargetMethods(Class<? extends Annotation> annotation) {
    // TODO: implement scanning order and shuffling at the same level properly. For now,
    // let's use JUnit infrastructure.

    // TODO: JUnit does not invoke @BeforeClass methods that are shadowed by static methods
    // in subclasses (!). I think this is wrong, but we can add a validation check for this
    // because it is counterintuitive. We can actually make it invalid to override or shadow
    // any hooks (?)
    return targetInfo.getAnnotatedMethods(annotation);
  }
}
