package com.carrotsearch.ant.tasks.junit4.events;

import java.io.IOException;

import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonReader;

/**
 * Events (messages) passed between the slave and the master.
 */
public enum EventType {
  BOOTSTRAP(BootstrapEvent.class),

  SUITE_STARTED(SuiteStartedEvent.class),
  SUITE_FAILURE(SuiteFailureEvent.class),
  SUITE_COMPLETED(SuiteCompletedEvent.class),

  APPEND_STDOUT(AppendStdOutEvent.class),
  APPEND_STDERR(AppendStdErrEvent.class),

  TEST_STARTED(TestStartedEvent.class),
  TEST_FAILURE(TestFailureEvent.class),
  TEST_IGNORED_ASSUMPTION(TestIgnoredAssumptionEvent.class),
  TEST_IGNORED(TestIgnoredEvent.class),
  TEST_FINISHED(TestFinishedEvent.class),

  IDLE(IdleEvent.class),
  QUIT(QuitEvent.class);

  /**
   * Concrete class associated with the given event type.
   */
  public final Class<? extends RemoteEvent> eventClass;

  /**
   * Initialize with concrete event class.
   */
  private EventType(Class<? extends RemoteEvent> eventClass) {
    this.eventClass = eventClass;
  }

  /**
   * Deserialize a given event type from stream. 
   */
  public RemoteEvent deserialize(JsonReader input) throws IOException {
    RemoteEvent empty;
    try {
      empty = eventClass.newInstance();
      empty.deserialize(input);
      return empty;
    } catch (InstantiationException e) {
      throw new IOException(e);
    } catch (IllegalAccessException e) {
      throw new IOException(e);
    }
  }
}
