package com.carrotsearch.ant.tasks.junit4.events.aggregated;

import java.util.Collections;
import java.util.List;

import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.SlaveID;
import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;

public class AggregatedSuiteResultEvent {
  private final SlaveID slave;
  private final Description description;
  private List<AggregatedTestResultEvent> tests;
  private List<FailureMirror> suiteFailures;

  public AggregatedSuiteResultEvent(SlaveID id, Description description, List<FailureMirror> suiteFailures, List<AggregatedTestResultEvent> tests) {
    this.slave = id;
    this.tests = tests;
    this.suiteFailures = suiteFailures;
    this.description = description;
  }

  public boolean isSuccessful() {
    if (!suiteFailures.isEmpty())
      return false;

    for (AggregatedTestResultEvent e : tests) {
      if (!e.isSuccessful()) {
        return false;
      }
    }

    return true;
  }

  public List<AggregatedTestResultEvent> getTests() {
    return tests;
  }

  public SlaveID getSlave() {
    return slave;
  }

  public Description getDescription() {
    return description;
  }

  public List<FailureMirror> getSuiteFailures() {
    return Collections.unmodifiableList(suiteFailures);
  }
}
