package com.carrotsearch.ant.tasks.junit4.events.aggregated;

import java.util.List;

import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;

public class SuiteExecutionResultEvent {

  private List<TestExecutionResultEvent> tests;
  private List<FailureMirror> suiteFailures;

  public SuiteExecutionResultEvent(List<FailureMirror> suiteFailures, List<TestExecutionResultEvent> tests) {
    this.tests = tests;
    this.suiteFailures = suiteFailures;
  }

  public boolean isSuccessful() {
    if (!suiteFailures.isEmpty())
      return false;

    for (TestExecutionResultEvent e : tests) {
      if (!e.isSuccessful()) {
        return false;
      }
    }

    return true;
  }

  public List<TestExecutionResultEvent> getTests() {
    return tests;
  }
}
