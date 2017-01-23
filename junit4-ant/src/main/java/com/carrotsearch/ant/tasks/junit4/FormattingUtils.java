package com.carrotsearch.ant.tasks.junit4;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.runner.Description;

/**
 * Formatting utilities for consistency across code.
 */
public final class FormattingUtils {
  /* */
  public static String padTo(int columns, String text, String ellipsis) {
    if (text.length() < columns) {
      return text;
    }

    text = ellipsis + text.substring(text.length() - (columns - ellipsis.length()));
    return text;
  }

  public static String formatTime(long timestamp) {
    return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT).format(new Date(timestamp));
  }

  public static String formatTimestamp(long ts) {
    return new SimpleDateFormat("HH:mm:ss.SSS", Locale.ROOT).format(new Date(ts));
  }

  public static String formatDurationInSeconds(long timeMillis) {
    final int precision;
    if (timeMillis >= 100 * 1000) {
      precision = 0;
    } else if (timeMillis >= 10 * 1000) {
      precision = 1;
    } else {
      precision = 2;
    }
    return String.format(Locale.ROOT, "%4." + precision + "fs", timeMillis / 1000.0);
  }

  public static String formatDescription(Description description) {
    return formatDescription(description, false);
  }

  public static String formatDescription(Description description, boolean fullNames) {
    StringBuilder buffer = new StringBuilder();
    String className = description.getClassName();
    if (className != null) {
      if (fullNames) {
        buffer.append(className);
      } else {
        String [] components = className.split("[\\.]");
        className = components[components.length - 1];
        buffer.append(className);
      }
      if (description.getMethodName() != null) { 
        buffer.append(".").append(description.getMethodName());
      } else {
        buffer.append(" (suite)");
      }
    } else {
      if (description.getMethodName() != null) {
        buffer.append(description.getMethodName());
      }
    }
    return buffer.toString();
  }  
}
