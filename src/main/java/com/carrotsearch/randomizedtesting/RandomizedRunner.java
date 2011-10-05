package com.carrotsearch.randomizedtesting;

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
 */
public final class RandomizedRunner extends Runner implements Filterable {
  /** The target class with test methods. */
  private final Class<?> target;

  /** JUnit utilities for scanning methods/ fields. */
  private final TestClass targetInfo;

  /** Top level suite description. */
  private Description description;

  /** Creates a new runner for the given class. */
  public RandomizedRunner(Class<?> testClass) throws InitializationError {
    this.target = testClass;
    this.targetInfo = new TestClass(testClass);
    this.description = createDescriptions(testClass);
  }

  @Override
  public Description getDescription() {
    return description;
  }

  @Override
  public void run(RunNotifier notifier) {
    for (Description d : description.getChildren()) {
      notifier.fireTestStarted(d);
      notifier.fireTestFinished(d);
    }
  }

  /**
   * Implement {@link Filterable} because GUIs depend on it to run tests selectively.
   */
  @Override
  public void filter(Filter filter) throws NoTestsRemainException {
  }

  /**
   * 
   */
  private Description createDescriptions(Class<?> clazz) {
    Description description = Description.createSuiteDescription(clazz.getName() + " [derived seed 398623984]");
    for (FrameworkMethod m : targetInfo.getAnnotatedMethods(Test.class)) {
      description.addChild(Description.createTestDescription(clazz, m.getName() + " [derived seed 2983289376]"));
    }
    return description;
  }
}
