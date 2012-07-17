package com.carrotsearch.randomizedtesting;

/**
 * This is thrown on uncaught exceptions during suite or test execution.
 */
@SuppressWarnings("serial")
final class UncaughtExceptionError extends Error {
  public UncaughtExceptionError(String message, Throwable cause) {
    super(message, cause);
  }
}
