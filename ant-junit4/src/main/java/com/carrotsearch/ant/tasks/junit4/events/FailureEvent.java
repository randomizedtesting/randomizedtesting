package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;

/**
 * Generic serialized failure event.
 */
public abstract class FailureEvent extends AbstractEvent {
  private FailureMirror failure;

  public FailureEvent(EventType type) {
    super(type);
  }

  protected void setFailure(Failure failure) {
    if (this.failure != null) {
      throw new IllegalStateException("Set only once.");
    }

    this.failure = new FailureMirror(failure);
  }

  public FailureMirror getFailure() {
    return failure;
  }
  
  public Description getDescription() {
    return failure.getDescription();
  }
}
