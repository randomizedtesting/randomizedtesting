package com.carrotsearch.randomizedtesting;

/**
 * What kind of container are we in? Unfortunately we need to adjust
 * to some "assumptions" containers make about runners.
 */
enum RunnerContainer {
  ECLIPSE,
  IDEA,
  UNKNOWN
}