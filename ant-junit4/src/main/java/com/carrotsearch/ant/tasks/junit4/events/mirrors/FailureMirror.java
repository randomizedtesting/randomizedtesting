package com.carrotsearch.ant.tasks.junit4.events.mirrors;

import java.io.*;

import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

@SuppressWarnings("serial")
public class FailureMirror extends SerializableMirror<Throwable> implements Serializable {
  private String message;
  private Description description;
  private String trace;
  private String throwableString;
  private String throwableClass;

  /** Was [@link Failure} an instance of {@link AssertionError}? */
  private boolean assertionViolation;
  private boolean assumptionViolation;

  public FailureMirror(Failure failure) {
    super(failure.getException());
    this.message = failure.getMessage();
    this.description = failure.getDescription();
    this.trace = failure.getTrace();
    this.assertionViolation = failure.getException() instanceof AssertionError;
    this.assumptionViolation = failure.getException() instanceof AssumptionViolatedException;
    this.throwableString = failure.getException().toString();
    this.throwableClass = failure.getException().getClass().getName();
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
    return super.getDeserialized();
  }
  
  /**
   * Returns an {@link ObjectOutputStream} serialized form of the original exception.
   */
  public byte[] getThrowableBytes() {
    return getBytes();
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
