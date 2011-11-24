package com.carrotsearch.ant.tasks.junit4.events.aggregated;

import java.util.Collections;
import java.util.List;

import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.SlaveID;
import com.carrotsearch.ant.tasks.junit4.events.IEvent;
import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;
import com.google.common.collect.Lists;

/**
 * A single test's execution information.
 */
public class AggregatedTestResultEvent {
  private final Description suite;
  private final Description description;
  private final SlaveID slave;

  private TestStatus status = TestStatus.OK;
  private List<FailureMirror> failures = Lists.newArrayList();

  private List<IEvent> eventStream;

  private boolean hasFailures;
  private boolean hasErrors;
  private boolean hasIgnoredAssumptions;

  public AggregatedTestResultEvent(SlaveID slave, Description suiteDescription, Description description) {
    this.description = description;
    this.suite = suiteDescription;
    this.slave = slave;
  }

  public Description getDescription() {
    return description;
  }
  
  public boolean isSuccessful() {
    return status == TestStatus.OK || 
           status == TestStatus.IGNORED ||
           status == TestStatus.IGNORED_ASSUMPTION;
  }

  public Description getSuiteDescription() {
    return suite;
  }

  public SlaveID getSlave() {
    return slave;
  }

  /**
   * Raw {@link IEvent} stream received during the duration of this test. 
   */
  public List<IEvent> getEventStream() {
    return Collections.unmodifiableList(eventStream);
  }

  /**
   * Exit status for this test.
   */
  public TestStatus getStatus() {
    return status;
  }
  
  void setIgnored() {
    assert status == TestStatus.OK;
    status = TestStatus.IGNORED;
  }

  void addFailure(FailureMirror failure) {
    failures.add(failure);

    hasFailures           |= failure.isAssertionViolation();
    hasIgnoredAssumptions |= failure.isAssumptionViolation();
    hasErrors             |= failure.isErrorViolation();
  }

  void complete(List<IEvent> eventStream) {
    this.eventStream = eventStream;

    if (hasErrors) {
      status = TestStatus.ERROR;
    } else if (hasFailures) {
      status = TestStatus.FAILURE;
    } else if (hasIgnoredAssumptions) {
      status = TestStatus.IGNORED_ASSUMPTION;
    }
  }
}
