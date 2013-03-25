package com.carrotsearch.ant.tasks.junit4.events.mirrors;

import java.io.*;

import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

/**
 * A type-safe mirror of {@link Failure}.
 */
public class FailureMirror {
  private String message;
  private String trace;
  private String throwableString;
  private String throwableClass;

  /** Was [@link Failure} an instance of an {@link AssertionError}? */
  private boolean assertionViolation;
  private boolean assumptionViolation;

  /** Serialized byte[] form of the original failure or null if it couldn't be serialized. */
  private SerializableMirror<Failure> serialized;

  /** The test {@link Description} that caused this failure. */
  private Description description;

  public FailureMirror(Failure failure) {
    this.serialized = SerializableMirror.of(failure);
    this.message = failure.getMessage();
    this.description = failure.getDescription();
    this.trace = failure.getTrace();

    final Throwable cause = failure.getException();
    this.assertionViolation = cause instanceof AssertionError;
    this.assumptionViolation = cause instanceof AssumptionViolatedException;
    this.throwableString = cause.toString();
    this.throwableClass = cause.getClass().getName();
  }

  public String getMessage() {
    return message;
  }

  public String getThrowableString() {
    return throwableString;
  }

  public Description getDescription() {
    return description;
  }

  public String getTrace() {
    return trace;
  }

  /**
   * Try to deserialize the failure's cause. Context class loader is used
   * for class lookup. May cause side effects (class loading, static blocks, etc.) 
   */
  public Throwable getThrowable() throws ClassNotFoundException, IOException {
    return serialized.getDeserialized().getException();
  }

  public boolean isAssumptionViolation() {
    return assumptionViolation;
  }

  public boolean isAssertionViolation() {
    return assertionViolation;
  }  

  public boolean isErrorViolation() {
    return isAssertionViolation() == false &&
           isAssumptionViolation() == false;
  }

  public String getThrowableClass() {
    return throwableClass;
  }
}
