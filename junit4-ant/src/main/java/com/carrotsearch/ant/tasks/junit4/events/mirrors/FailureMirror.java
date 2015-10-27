package com.carrotsearch.ant.tasks.junit4.events.mirrors;

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

  /** The test {@link Description} that caused this failure. */
  private Description description;

  public FailureMirror(Description description,
                       String message, 
                       String trace,
                       String throwableString,
                       String throwableClass,
                       boolean assertionViolation,
                       boolean assumptionViolation) {
    this.message = message;
    this.trace = trace;
    this.throwableString = throwableString;
    this.throwableClass = throwableClass;
    this.assertionViolation = assertionViolation;
    this.assumptionViolation = assumptionViolation;
    this.description = description;
  }

  public FailureMirror(Failure failure) {
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
