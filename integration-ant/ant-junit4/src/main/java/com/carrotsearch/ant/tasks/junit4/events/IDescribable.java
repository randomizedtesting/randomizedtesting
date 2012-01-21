package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;

/**
 * An event that carries a {@link Description}.
 */
public interface IDescribable {
  Description getDescription();
}
