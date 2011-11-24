package com.carrotsearch.ant.tasks.junit4.listeners;

import com.carrotsearch.ant.tasks.junit4.events.TestFinishedEvent;
import com.google.common.eventbus.Subscribe;

/**
 * A listener that will subscribe to test execution and dump
 * informational info about the progress to the console.
 */
public class ConsoleInfoListener {
  @Subscribe
  public void summary(TestFinishedEvent e) {
    System.out.println("Test finished: " + e.getDescription().getMethodName());
  }
}
