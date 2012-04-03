package com.carrotsearch.randomizedtesting;

import java.util.*;

/**
 * Utilities for dealing with throwables, stacks, etc.
 */
public final class TraceFormatting {
  /** Stack filtering prefixes. */
  private final List<String> filteredPrefixes;

  /**
   * Default stack traces, no filtering.
   */
  public TraceFormatting() {
    this(Collections.<String> emptyList());
  }

  public TraceFormatting(List<String> filteredPrefixes) {
    this.filteredPrefixes = filteredPrefixes;
  }
  
  /** Format an exception and all of its nested stacks into a string. */
  public String formatThrowable(Throwable t) {
    return formatThrowable(new StringBuilder(), t).toString();
  }

  /** Format an exception and all of its nested stacks into a string. */
  public StringBuilder formatThrowable(StringBuilder b, Throwable t) {
    b.append(t.toString()).append("\n");
    formatStackTrace(b, t.getStackTrace());
    if (t.getCause() != null) {
      b.append("Caused by: ");
      formatThrowable(b, t.getCause());
    }
    return b;
  }

  /** Format a list of stack entries into a string. */
  public StringBuilder formatStackTrace(StringBuilder b, StackTraceElement[] stackTrace) {
    return formatStackTrace(b, Arrays.asList(stackTrace));
  }

  public String formatStackTrace(StackTraceElement[] stackTrace) {
    return formatStackTrace(Arrays.asList(stackTrace));
  }

  public String formatStackTrace(Iterable<StackTraceElement> stackTrace) {
    return formatStackTrace(new StringBuilder(), stackTrace).toString();
  }

  /** Format a list of stack entries into a string. */
  public StringBuilder formatStackTrace(StringBuilder b, Iterable<StackTraceElement> stackTrace) {
    Set<String> filteredSet = new HashSet<String>();
    for (StackTraceElement e : stackTrace) {
      String stackLine = e.toString();

      String filtered = null;
      for (String prefix : filteredPrefixes) {
        if (stackLine.startsWith(prefix)) {
          filtered = prefix; 
          break;
        }
      }

      if (filtered != null) {
        if (filteredSet.isEmpty()) {
          b.append("    [...");
        }
        filteredSet.add(filtered);
      } else {
        appendFiltered(b, filteredSet);
        b.append("    ").append(stackLine).append("\n");
      }
    }
    appendFiltered(b, filteredSet);
    return b;
  }

  private static void appendFiltered(StringBuilder b, Set<String> filteredSet) {
    if (!filteredSet.isEmpty()) {
      boolean first = true;
      for (String prefix : filteredSet) {
        if (!first) {
          b.append(", ");
        }
        first = false;
        b.append(prefix).append("*");
      }
      b.append("]\n");
      filteredSet.clear();
    }
  }
}
