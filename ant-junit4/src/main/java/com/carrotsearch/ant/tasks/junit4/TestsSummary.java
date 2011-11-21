package com.carrotsearch.ant.tasks.junit4;

/**
 * Summary of tests execution.
 */
public class TestsSummary {
  public final int tests, failures, errors, assumptions, ignores;

  public TestsSummary(int tests, int failures, int errors, int assumptions, int ignores) {
    this.tests = tests;
    this.failures = failures;
    this.errors = errors;
    this.assumptions = assumptions;
    this.ignores = ignores;
  }

  public boolean isSuccessful() {
    return (errors + failures) == 0;
  }
  
  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append(tests).append(pluralize(tests, " test"));
    if (errors > 0) s.append(", ").append(errors).append(pluralize(errors, " error"));
    if (failures > 0) s.append(", ").append(failures).append(pluralize(failures, " failure"));
    if (ignores + assumptions > 0) {
      s.append(", ").append(ignores + assumptions).append(" ignored");
      if (assumptions > 0) {
        s.append(" (").append(assumptions).append(pluralize(assumptions, " assumptions)"));
      }
    }
    return s.toString();
  }

  private String pluralize(int count, String word) {
    if (count != 1) {
      word += "s";
    }
    return word;
  }
}
