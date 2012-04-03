package com.carrotsearch.randomizedtesting;

import org.junit.After;
import org.junit.AfterClass;

/**
 * Lifecycle stages for tracking resources.
 */
public enum LifecycleScope {
  /**
   * A single test case, including all {@link After} hooks.
   */
  TEST,
  
  /**
   * A single suite (class), including all {@link AfterClass} hooks.
   */
  SUITE;
}
