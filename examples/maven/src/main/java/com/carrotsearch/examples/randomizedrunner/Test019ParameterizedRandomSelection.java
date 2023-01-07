package com.carrotsearch.examples.randomizedrunner;

import com.carrotsearch.randomizedtesting.RandomizedContext;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Name;
import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import com.carrotsearch.randomizedtesting.rules.TestRuleAdapter;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This shows how to implement test factory together with a randomized execution of a subset of
 * tests.
 *
 * @see "https://github.com/randomizedtesting/randomizedtesting/issues/308"
 */
public class Test019ParameterizedRandomSelection extends RandomizedTest {
  private AtomicBoolean shouldRun;
  private String testcase;

  public Test019ParameterizedRandomSelection(
      @Name("testcase") String testcase, AtomicBoolean shouldRun) {
    this.testcase = testcase;
    this.shouldRun = shouldRun;
  }

  @Before
  void shouldRun() {
    Assume.assumeTrue("This test is ignored for this seed.", shouldRun.get());
  }

  @Test
  public void runTest() {
    System.out.println("Testing: " + testcase);
  }

  /** A rule that helps in dynamic test selection. */
  public static class DynamicTestSubsetRule extends TestRuleAdapter implements Iterable<Object[]> {
    private final LinkedHashMap<Object[], AtomicBoolean> parameters = new LinkedHashMap<>();

    public DynamicTestSubsetRule(Iterable<Object[]> parameters) {
      for (Object[] params : parameters) {
        Object[] paramsAndShouldRun = Arrays.copyOf(params, params.length + 1);
        AtomicBoolean shouldRun = new AtomicBoolean();
        paramsAndShouldRun[params.length] = shouldRun;
        this.parameters.put(paramsAndShouldRun, shouldRun);
      }
    }

    @Override
    public Iterator<Object[]> iterator() {
      return parameters.keySet().iterator();
    }

    @Override
    protected void before() throws Throwable {
      super.before();

      // We now have access to the randomized context so we can pick the tests that should
      // run. The selection can be done in many ways; here we pick 10 random tests from the pool.
      int maxTests = 10;
      List<AtomicBoolean> subset = new ArrayList<>(parameters.values());
      if (subset.size() > maxTests) {
        Collections.shuffle(subset, RandomizedContext.current().getRandom());
        subset = subset.subList(0, maxTests);
      }

      for (AtomicBoolean shouldRun : subset) {
        shouldRun.set(true);
      }
    }
  }

  @ClassRule public static DynamicTestSubsetRule selectTests;

  /**
   * Test parameters factory does not have access to any {@link
   * com.carrotsearch.randomizedtesting.RandomizedContext} because it runs prior to anything else.
   * We collect all tests and save a map of them to the class so that {@link #selectTests} can pick
   * a random subset prior to running anything and so that all tests (even those not selected for
   * execution) are shown and reported.
   */
  @ParametersFactory(argumentFormatting = "testCase: %s")
  public static Iterable<Object[]> generateTestCases() {
    ArrayList<Object[]> testCases = new ArrayList<>();
    for (int i = 0; i < 50; i++) {
      testCases.add(new Object[] {"#" + (i + i)});
    }

    selectTests = new DynamicTestSubsetRule(testCases);
    return selectTests;
  }
}
