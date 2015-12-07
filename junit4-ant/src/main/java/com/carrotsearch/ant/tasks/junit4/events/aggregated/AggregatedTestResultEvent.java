package com.carrotsearch.ant.tasks.junit4.events.aggregated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.ForkedJvmInfo;
import com.carrotsearch.ant.tasks.junit4.events.IEvent;
import com.carrotsearch.ant.tasks.junit4.events.TestFinishedEvent;
import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;

/**
 * A single test's execution information.
 */
public class AggregatedTestResultEvent implements AggregatedResultEvent {
  private final Description suite;
  private final Description description;
  private final ForkedJvmInfo slave;

  private TestStatus status = TestStatus.OK;
  private List<FailureMirror> failures = new ArrayList<>();

  private List<IEvent> eventStream;

  private boolean hasFailures;
  private boolean hasErrors;
  private boolean hasIgnoredAssumptions;

  /** If {@link #status} is {@link TestStatus#IGNORED} then this contains the cause. */
  private String ignoreCause;
  
  /** Associated {@link TestFinishedEvent}. */
  private TestFinishedEvent testFinishedEvent;

  public AggregatedTestResultEvent(ForkedJvmInfo slave, Description suiteDescription, Description description) {
    this.description = description;
    this.suite = suiteDescription;
    this.slave = slave;
  }

  @Override
  public Description getDescription() {
    return description;
  }
  
  @Override
  public boolean isSuccessful() {
    return status == TestStatus.OK || 
           status == TestStatus.IGNORED ||
           status == TestStatus.IGNORED_ASSUMPTION;
  }

  public Description getSuiteDescription() {
    return suite;
  }

  @Override
  public ForkedJvmInfo getSlave() {
    return slave;
  }

  @Override
  public List<FailureMirror> getFailures() {
    return Collections.unmodifiableList(failures);
  }

  /**
   * Execution time in millis.
   */
  public long getExecutionTime() {
    return testFinishedEvent.getExecutionTime();
  }

  /**
   * Execution start timestamp (on the slave).
   */
  public long getStartTimestamp() {
    return testFinishedEvent.getStartTimestamp();
  }

  /**
   * Raw {@link IEvent} stream received during the duration of this test. 
   */
  @Override
  public List<IEvent> getEventStream() {
    if (eventStream == null)
      throw new RuntimeException("Unfinished test?" + suite + ", " + description);
    return Collections.unmodifiableList(eventStream);
  }

  /**
   * Exit status for this test.
   */
  public TestStatus getStatus() {
    return status;
  }
  
  public String getCauseForIgnored() {
    return ignoreCause;
  }
  
  void setIgnored(String cause) {
    assert status == TestStatus.OK;
    status = TestStatus.IGNORED;
    ignoreCause = cause;
  }

  void addFailure(FailureMirror failure) {
    failures.add(failure);

    hasFailures           |= failure.isAssertionViolation();
    hasIgnoredAssumptions |= failure.isAssumptionViolation();
    hasErrors             |= failure.isErrorViolation();
  }

  void complete(TestFinishedEvent e, List<IEvent> eventStream) {
    this.eventStream = eventStream;
    this.testFinishedEvent = e;

    if (hasErrors) {
      status = TestStatus.ERROR;
    } else if (hasFailures) {
      status = TestStatus.FAILURE;
    } else if (hasIgnoredAssumptions) {
      status = TestStatus.IGNORED_ASSUMPTION;
    }
  }

  /**
   * This isn't a nice hack but it allows associating {@link TestFinishedEvent} and
   * {@link AggregatedTestResultEvent} by identity. = 
   */
  public TestFinishedEvent getTestFinishedEvent() {
    return testFinishedEvent;
  }
}
