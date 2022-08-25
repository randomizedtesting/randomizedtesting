package com.carrotsearch.randomizedtesting;

import java.util.concurrent.TimeUnit;

public final class DeadlineClock {
  private final long startTime;
  private final long durationNanos;

  public DeadlineClock(TimeUnit unit, long duration) {
    startTime = System.nanoTime();
    durationNanos =  unit.toNanos(duration);
  }

  public boolean isBeforeDeadline() {
    return !isAfterDeadline();
  }

  public boolean isAfterDeadline() {
    return System.nanoTime() - startTime >= durationNanos;
  }

  public long timeUntilDeadline(TimeUnit unit) {
    long elapsedNanos = System.nanoTime() - startTime;
    if (elapsedNanos >= durationNanos) {
      return 0;
    } else {
      return unit.convert(durationNanos - elapsedNanos, TimeUnit.NANOSECONDS);
    }
  }
}
