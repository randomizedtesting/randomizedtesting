package com.carrotsearch.randomizedtesting;

/**
 * This is never thrown. The only purpose it serves it to carry chained stack traces
 * for informational purposes.
 */
@SuppressWarnings("serial")
final class NotAnException extends Throwable {
  public NotAnException(String message) {
    super(message);
  }
}
