package com.carrotsearch.ant.tasks.junit4.events.aggregated;

import java.util.Collections;
import java.util.List;

import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.ForkedJvmInfo;
import com.carrotsearch.ant.tasks.junit4.events.IEvent;
import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;

public class AggregatedSuiteResultEvent implements AggregatedResultEvent {
  private transient final ForkedJvmInfo slave;

  private final long executionTime;
  private final long startTimestamp;
  private final Description description;

  private final List<AggregatedTestResultEvent> tests;
  private final List<FailureMirror> suiteFailures;
  private final List<IEvent> eventStream;

  private final AggregatedSuiteStartedEvent startEvent;

  public AggregatedSuiteResultEvent(
      AggregatedSuiteStartedEvent startEvent,
      ForkedJvmInfo id, 
      Description description, 
      List<FailureMirror> suiteFailures, 
      List<AggregatedTestResultEvent> tests,
      List<IEvent> eventStream,
      long startTimestamp, 
      long executionTime) {
    this.startEvent = startEvent;
    this.slave = id;
    this.tests = tests;
    this.suiteFailures = suiteFailures;
    this.description = description;
    this.eventStream = eventStream;
    this.executionTime = executionTime;
    this.startTimestamp = startTimestamp;
  }

  public AggregatedSuiteStartedEvent getStartEvent() {
    return startEvent;
  }
  
  public List<AggregatedTestResultEvent> getTests() {
    return tests;
  }

  @Override
  public List<FailureMirror> getFailures() {
    return Collections.unmodifiableList(suiteFailures);
  }

  @Override
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

  @Override
  public List<IEvent> getEventStream() {
    return eventStream;
  }
  
  @Override
  public ForkedJvmInfo getSlave() {
    return slave;
  }

  @Override
  public Description getDescription() {
    return description;
  }

  /**
   * Execution time in milliseconds.
   */
  public long getExecutionTime() {
    return executionTime;
  }

  /**
   * Execution start timestamp (on the slave).
   */
  public long getStartTimestamp() {
    return startTimestamp;
  }

  /**
   * The number of tests that have {@link TestStatus#FAILURE} and
   * include assertion violations at suite level.
   */
  public int getFailureCount() {
    int count = 0;
    for (AggregatedTestResultEvent t : getTests()) {
      if (t.getStatus() == TestStatus.FAILURE)
        count++;
    }
    for (FailureMirror m : getFailures()) {
      if (m.isAssertionViolation())
        count++;
    }
    return count;
  }
  
  /**
   * The number of tests that have {@link TestStatus#ERROR} and
   * include the suite-level errors.
   */
  public int getErrorCount() {
    int count = 0;
    for (AggregatedTestResultEvent t : getTests()) {
      if (t.getStatus() == TestStatus.ERROR)
        count++;
    }
    
    for (FailureMirror m : getFailures()) {
      if (m.isErrorViolation())
        count++;
    }
    return count;
  }

  /**
   * Return the number of ignored or assumption-ignored tests.
   */
  public int getIgnoredCount() {
    int count = 0;
    for (AggregatedTestResultEvent t : getTests()) {
      if (t.getStatus() == TestStatus.IGNORED ||
          t.getStatus() == TestStatus.IGNORED_ASSUMPTION) {
        count++;
      }
    }
    return count;
  }
}
