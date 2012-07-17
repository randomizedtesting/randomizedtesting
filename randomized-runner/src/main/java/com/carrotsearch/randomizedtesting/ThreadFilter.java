package com.carrotsearch.randomizedtesting;

import com.carrotsearch.randomizedtesting.annotations.ThreadLeakFilters;

/**
 * @see ThreadLeakFilters 
 */
public interface ThreadFilter {
  /**
   * @return Return <code>true</code> if thread <code>t</code> should be
   * filtered out.
   */
  public boolean reject(Thread t);
}