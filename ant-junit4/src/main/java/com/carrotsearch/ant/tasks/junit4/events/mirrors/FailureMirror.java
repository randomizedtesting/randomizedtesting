package com.carrotsearch.ant.tasks.junit4.events.mirrors;

import java.io.*;

import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

@SuppressWarnings("serial")
public class FailureMirror implements Serializable {
  private String message;
  private Description description;
  private String trace;
  private byte[] exception;
  
  /** Was [@link Failure} an instance of {@link AssertionError}? */
  private boolean assertionViolation;
  private boolean assumptionViolation;

  public FailureMirror(Failure failure) {
    this.message = failure.getMessage();
    this.description = failure.getDescription();
    this.trace = failure.getTrace();
    this.exception = tryToSerialize(failure.getException());
    this.assertionViolation = failure.getException() instanceof AssertionError;
    this.assumptionViolation = failure.getException() instanceof AssumptionViolatedException;
  }

  public String getMessage() {
    return message;
  }
  
  public Description getDescription() {
    return description;
  }
  
  public String getTrace() {
    return trace;
  }
  
  /**
   * Returns an {@link ObjectOutputStream} serialized form of the original exception.
   */
  public byte[] getThrowableBytes() {
    return exception;
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

  /**
   * Attempt to reinstantiate the exception from serialized bytes.
   */
  public Throwable getThrowable() throws ClassNotFoundException, IOException {
    if (exception == null)
      return null;
    
    ObjectInputStream is = new ObjectInputStream(
        new ByteArrayInputStream(exception));
    return (Throwable) is.readObject();
  }

  private static byte[] tryToSerialize(Throwable t) {
    if (t != null) {
      try {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(t);
        oos.close();
        return os.toByteArray();
      } catch (Throwable ignore) {
        // Ignore.
      }
    }
    return null;
  }
}
