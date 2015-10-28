package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;

/**
 * An event that carries a {@link Description}.
 */
public interface IDescribable {
  /*
   * TODO: [GH-211] we should just pass over the essential information about
   * a test, without exposing Class<?>, Description or Annotation instances (which
   * are problematic to serialize, initialize, etc.)
   */
  Description getDescription();
}
