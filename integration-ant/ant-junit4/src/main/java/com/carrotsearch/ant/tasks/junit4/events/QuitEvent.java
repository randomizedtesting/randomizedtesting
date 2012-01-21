package com.carrotsearch.ant.tasks.junit4.events;

/**
 * Final message sent from the slave. Also signals orderly shutdown.
 */
public class QuitEvent extends AbstractEvent {
  public QuitEvent() {
    super(EventType.QUIT);
  }
}
