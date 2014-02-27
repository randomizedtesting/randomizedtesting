package com.carrotsearch.ant.tasks.junit4.events.aggregated;

import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.ForkedJvmInfo;

/**
 * High level heartbeat event issued to report listeners when a forked JVM
 * does not repond for a longer while. The {@link #getDescription} method should
 * return an approximate place where the forked JVM is at the moment, but this is
 * not guaranteed (and may be null).
 */
public final class HeartBeatEvent {
  private final ForkedJvmInfo slave;
  private final Description description;
  private final long lastActivity;
  private final long currentTime;

  public HeartBeatEvent(ForkedJvmInfo slave, Description description, long lastActivity, long currentTime) {
    this.slave = slave;
    this.description = description;
    this.lastActivity = lastActivity;
    this.currentTime = currentTime;
  }
  
  public Description getDescription() {
    return description;
  }
  
  public long getCurrentTime() {
    return currentTime;
  }
  
  public long getLastActivity() {
    return lastActivity;
  }

  public long getNoEventDuration() {
    return getCurrentTime() - getLastActivity();
  }

  public ForkedJvmInfo getSlave() {
    return slave;
  }
}
