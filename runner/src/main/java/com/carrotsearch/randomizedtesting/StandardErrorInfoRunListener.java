package com.carrotsearch.randomizedtesting;

import java.util.Arrays;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * A {@link RunListener} that reports failed tests to standard error, along with the seed
 * information and switches that may be helpful to reproduce the test case.
 */
public class StandardErrorInfoRunListener extends RunListener {
  @Override
  public void testStarted(Description description) throws Exception {
    System.err.println("**  Test name: " + description.getMethodName());
    System.err.flush();
  }

  @Override
  public void testAssumptionFailure(Failure failure) {
    System.err.println("** IGNORED (assumption): " + failure.getDescription().getMethodName());
    System.err.flush();
  }

  @Override
  public void testIgnored(Description description) throws Exception {
    System.err.println("** IGNORED: " + description.getMethodName());
    System.err.flush();
  }

  @Override
  public void testFailure(Failure failure) throws Exception {
    Description d = failure.getDescription();

    // This isn't too pretty... rewrite later.
    StringBuilder b = new StringBuilder();
    b.append("** FAILED   : ").append(d.getDisplayName()).append("\n");
    b.append("   Message  : " + failure.getMessage() + "\n");
    b.append("   Reproduce:");
    b.append(" -D").append(RandomizedRunner.SYSPROP_RANDOM_SEED).append("=").append(RandomizedContext.current().getRunnerSeed());
    if (d.getClassName() != null)
      b.append(" -D").append(RandomizedRunner.SYSPROP_TESTCLASS).append("=").append(d.getClassName());
    if (d.getMethodName() != null)
      b.append(" -D").append(RandomizedRunner.SYSPROP_TESTMETHOD).append("=").append(RandomizedRunner.stripSeed(d.getMethodName()));

    for (String p : Arrays.asList(RandomizedRunner.SYSPROP_ITERATIONS, RandomizedRunner.SYSPROP_NIGHTLY)) {
      if (System.getProperty(p) != null) {
        b.append(" -D").append(p).append("=").append(System.getProperty(p));
      }
    }
    b.append("\n");

    b.append("   Throwable: " + failure.getTrace());
    b.append("\n");

    System.err.println(b.toString());
    System.err.flush();
  }
}
