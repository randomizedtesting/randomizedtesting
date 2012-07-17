package com.carrotsearch.randomizedtesting;

/**
 * This is never thrown. The only purpose it serves it to carry chained stack traces
 * for informational purposes.
 */
@SuppressWarnings("serial")
final class StackTraceHolder extends Throwable {
  public StackTraceHolder(String message) {
    super(message);
  }
}
