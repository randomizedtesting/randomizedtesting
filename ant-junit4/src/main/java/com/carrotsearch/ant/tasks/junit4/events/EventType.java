package com.carrotsearch.ant.tasks.junit4.events;

/**
 * Events (messages) passed between the slave and the master.
 */
public enum EventType {
  BOOTSTRAP(BootstrapEvent.class),

  APPEND_STDOUT(AppendStdOutEvent.class),
  APPEND_STDERR(AppendStdErrEvent.class),

  SUITE_STARTED(SuiteStartedEvent.class),
  SUITE_FAILURE(SuiteFailureEvent.class),
  SUITE_COMPLETED(SuiteCompletedEvent.class),

  TEST_STARTED(TestStartedEvent.class),
  TEST_FAILURE(TestFailureEvent.class),
  TEST_IGNORED_ASSUMPTION(TestIgnoredAssumptionEvent.class),
  TEST_IGNORED(TestIgnoredEvent.class),
  TEST_FINISHED(TestFinishedEvent.class),

  QUIT(QuitEvent.class);

  /**
   * Concrete class associated with the given event type.
   */
  public final Class<? extends IEvent> eventClass;

  /**
   * Initialize with concrete event class.
   */
  private EventType(Class<? extends IEvent> eventClass) {
    this.eventClass = eventClass;
  }
}
