package com.carrotsearch.randomizedtesting;

/**
 * Provide a test instance.
 */
interface InstanceProvider {
  Object newInstance() throws Throwable;
}
