package com.carrotsearch.randomizedtesting;

import org.hamcrest.Description;

/**
 * We have our own "custom" assumption class because of JUnit's internal closed-up architecture.
 * 
 * <p>We currently subclass and substitute JUnit's internal AVE, but we could as well have our
 * own exception and handle it properly in {@link RandomizedRunner}.
 */
@SuppressWarnings("serial") 
class InternalAssumptionViolatedException extends org.junit.internal.AssumptionViolatedException {
  private final String message;

  public InternalAssumptionViolatedException(String message) {
    this(message, null);
  }

  public InternalAssumptionViolatedException(String message, Throwable t) {
    super(t, /* no matcher. */ null);
    if (getCause() != t) {
      throw new Error("AssumptionViolationException not setting up cause properly. Panic.");
    }
    this.message = message;
  }

  @Override
  public String getMessage() {
    return super.getMessage();
  }
  
  @Override
  public void describeTo(Description description) {
    if (message == null || message.trim().length() == 0) {
      description.appendText("failed assumption");
    } else {
      description.appendText(message);
    }
    if (getCause() != null) {
      description.appendText("(throwable: " + getCause().toString() + ")");
    }
  }
}