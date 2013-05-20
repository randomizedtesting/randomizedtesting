package com.carrotsearch.randomizedtesting;

/**
 * Rethrowing checked exceptions as unchecked ones. Eh, it is sometimes useful...
 */
public final class Rethrow {
  /**
   * Classy puzzler to rethrow any checked exception as an unchecked one.
   */
  @SuppressWarnings("all")
  private static class Rethrower<T extends Throwable> {
    private void rethrow(Throwable t) throws T {
      throw (T) t;
    }
  }
  
  /**
   * Rethrows <code>t</code> (identical object).
   */
  public static RuntimeException rethrow(Throwable t) {
    new Rethrower<Error>().rethrow(t);
    // Inaccessible.
    return null;
  }
}

