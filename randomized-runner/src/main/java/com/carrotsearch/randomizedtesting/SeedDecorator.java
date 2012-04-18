package com.carrotsearch.randomizedtesting;

import com.carrotsearch.randomizedtesting.annotations.SeedDecorators;

/**
 * This is an advanced feature. See {@link SeedDecorators} annotation.
 */
public interface SeedDecorator {
  /**
   * Called once after instantiation to set up the decorator. 
   */
  void initialize(Class<?> suiteClass);

  /**
   * Called to decorate the initial seed for a {@link Randomness}.
   */
  long decorate(long seed);
}
