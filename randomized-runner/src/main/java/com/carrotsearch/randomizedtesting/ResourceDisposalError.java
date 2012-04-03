package com.carrotsearch.randomizedtesting;

/**
 * Thrown when a resource could not be released.
 * 
 * @see RandomizedContext#closeAtEnd(java.io.Closeable, LifecycleScope)
 */
@SuppressWarnings("serial")
class ResourceDisposalError extends Error {
  public ResourceDisposalError(String msg, Throwable cause) {
    super(msg, cause);
  }
}
