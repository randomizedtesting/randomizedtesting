package com.carrotsearch.ant.tasks.junit4;

import java.util.Locale;

public final class Duration {
  /**
   * Format a duration in milliseconds to a human string (in English).
   */
  public static CharSequence toHumanDuration(long duration) {
    final long MILLIS_IN_A_SECOND = 1000;
    final long MILLIS_IN_A_MINUTE = MILLIS_IN_A_SECOND * 60;
    final long MILLIS_IN_AN_HOUR  = MILLIS_IN_A_MINUTE * 60;
    final long MILLIS_IN_A_DAY    = MILLIS_IN_AN_HOUR * 24;

    boolean longTime = (duration >= MILLIS_IN_A_SECOND * 10); 

    StringBuilder str = new StringBuilder();
    duration = emitOrSkip(duration, str, MILLIS_IN_A_DAY, " day", true);
    duration = emitOrSkip(duration, str, MILLIS_IN_AN_HOUR, " hour", true);
    duration = emitOrSkip(duration, str, MILLIS_IN_A_MINUTE, " minute", true);
    if (longTime) {
      duration = emitOrSkip(duration, str, MILLIS_IN_A_SECOND, " second", true);
    } else {
      str.append(String.format(Locale.ROOT, "%.2f sec.", (duration / 1000.0f)));
    }
    return str;
  }

  private static long emitOrSkip(long value, StringBuilder str, long unit, String unitName, boolean skipEmpty) {
    final long units = value / unit;
    if (units != 0 || !skipEmpty) {
      if (str.length() > 0) str.append(" ");
      str.append(units)
         .append(Pluralize.pluralize((int) units, unitName));
    }

    value -= units * unit;
    return value;
  }  
}
