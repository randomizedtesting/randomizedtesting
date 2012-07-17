package com.carrotsearch.randomizedtesting;

/**
 * A thread went wild.
 */
@SuppressWarnings("serial")
final class ThreadLeakError extends Error {
  public ThreadLeakError(String message) {
    super(message);
  }
}
