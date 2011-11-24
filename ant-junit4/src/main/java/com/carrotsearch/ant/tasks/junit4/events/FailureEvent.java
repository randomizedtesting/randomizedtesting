package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;

/**
 * Generic serialized failure event.
 */
@SuppressWarnings("serial")
public abstract class FailureEvent extends AbstractEvent {
  private FailureMirror failure;

  public FailureEvent(EventType type, Failure failure) {
    super(type);
    this.failure = new FailureMirror(failure);
  }

  public FailureMirror getFailure() {
    return failure;
  }

  public Description getDescription() {
    return failure.getDescription();
  }
}
