package com.carrotsearch.randomizedtesting;


import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

// TODO: eclipse doesn't want to link []-postfixed Descriptions to source code. Anything we can do about it?

/**
 * A somewhat less hairy, no-fancy design, {@link Runner} implementation for 
 * running randomized tests.
 * 
 * <p>Supports the following JUnit4 features:
 * <ul>
 *   <li>{@link BeforeClass}-annotated methods (before all tests of a class/superclass),</li>
 *   <li>{@link Before}-annotated methods (before each test),</li>
 *   <li>{@link Test}-annotated methods,</li>
 *   <li>{@link After}-annotated methods (after each test),</li>
 *   <li>{@link AfterClass}-annotated methods (after all tests of a class/superclass),</li>
 *   <li>{@link Rule}-annotated fields implementing {@link MethodRule}.</li>
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
 *   <li>test methods are allowed to return values (they are ignored),</li>
 * </ul>
 */
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

    // Initialize the runner's master seed/ randomness source.
    final String globalSeed = System.getProperty(SYSPROP_RANDOM_SEED);
    if (globalSeed != null) {
      final long[] seedChain = parseSeedChain(globalSeed);
      if (seedChain.length == 0 || seedChain.length > 2)
        throw new IllegalArgumentException("Invalid " + SYSPROP_RANDOM_SEED + " specification: " + globalSeed);
      
      if (seedChain.length > 1)
        testRandomnessOverride = new Randomness(seedChain[1]);
      runnerRandomness = new Randomness(seedChain[0]);
    } else {
      runnerRandomness = new Randomness(System.currentTimeMillis());
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

  @Override
  public void run(RunNotifier notifier) {
    for (TestCandidate c : testCandidates) {
      if (filter != null && !filter.shouldRun(c.description)) {
        continue;
      }

      notifier.fireTestStarted(c.description);
      // TODO: execute test.
      notifier.fireTestFinished(c.description);
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
    List<FrameworkMethod> testMethods = targetInfo.getAnnotatedMethods(Test.class);
    List<TestCandidate> candidates = new ArrayList<TestCandidate>();
    for (FrameworkMethod method : testMethods) {
      Validation.checkThat(method.getMethod())
        .describedAs("Test method " + target.getName() + "#" + method.getName())
        .isPublic()
        .hasArgsCount(0);

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
   * and a hash of their name (so that two identical methods have different 
   * runtime randomness). Take into account global override and method and class level
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
      return Long.parseLong(seed.value(), 16);
    }
    if ((seed = target.getAnnotation(Seed.class)) != null) {
      return Long.parseLong(seed.value(), 16);
    }
    return runnerRandomness.seed ^ method.getName().hashCode();
  }

  /**
   * Determine if a given method's iterations should run with a fixed seed or not.
   */
  private boolean isConstantSeedForAllIterations(FrameworkMethod method) {
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
   * Formats randomness seed or seeds into something the user can type in to get predictably repeatable
   * execution.
   */
  private static String formatSeedChain(Randomness... randomnesses) {
    // TODO: use base64-like encoding to make them shorter and get rid of the '-' character.
    StringBuilder b = new StringBuilder();
    b.append("[");
    for (int i = 0; i < randomnesses.length; i++) {
      if (i > 0) b.append(":");
      b.append(Long.toString(randomnesses[i].seed, 16));
    }
    b.append("]");
    return b.toString();
  }

  /**
   * Parse a seed chain formatted with {@link #formatSeedChain(Randomness...)}. 
   */
  private static long [] parseSeedChain(String chain) {
    if (!chain.matches("[0-9A-Za-z\\:]+")) {
      throw new IllegalArgumentException("Not a valid seed chain: " + chain);
    }
    String [] splits = chain.split("[\\:]");
    long [] longs = new long [splits.length];
    for (int i = 0; i < splits.length; i++)
      longs[i] = Long.parseLong(splits[i], 16);
    return longs;
  }
}
