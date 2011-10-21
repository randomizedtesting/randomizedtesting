package com.carrotsearch.randomizedtesting;

/**
 * A thread went wild.
 */
@SuppressWarnings("serial")
final class ThreadingError extends Error {
  public ThreadingError(String message) {
    super(message);
  }
}
