package com.carrotsearch.ant.tasks.junit4.listeners;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.SlaveID;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedSuiteResultEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedTestResultEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.TestStatus;
import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;

/**
 * A listener that will subscribe to test execution and dump
 * informational info about the progress to the console.
 */
public class ConsoleReport implements AggregatedEventListener {
  private static EnumMap<TestStatus, String> statusNames;
  static {
    statusNames = Maps.newEnumMap(TestStatus.class);
    for (TestStatus s : TestStatus.values()) {
      statusNames.put(s,
          s == TestStatus.IGNORED_ASSUMPTION
          ? "IGNOR/A" : s.toString());
    }
  }

  @Subscribe
  public void singleTest(AggregatedTestResultEvent e) {
    format(e.getSlave(), e.getDescription(), e.getStatus(), e.getExecutionTime(), e.getFailures());
  }

  @Subscribe
  public void suite(AggregatedSuiteResultEvent e) {
    if (!e.getSuiteFailures().isEmpty()) {
      format(e.getSlave(), e.getDescription(), TestStatus.ERROR, 0, e.getSuiteFailures());
    }
  }

  private void format(SlaveID slave, Description description,
      TestStatus status, int timeMillis, List<FailureMirror> failures) {
    StringBuilder line = new StringBuilder();

    if (slave.slaves > 1) {
      line.append("S").append(slave.id).append(" ");
    }
    line.append(formatTime(timeMillis));
    line.append(Strings.padEnd(statusNames.get(status), 7, ' '));
    line.append(" | ");

    String className = description.getClassName();
    if (className != null) {
      String [] components = className.split("[\\.]");
      className = components[components.length - 1];
      line.append(className);
      if (description.getMethodName() != null) { 
        line.append(".");
      } else {
        line.append(" (suite)");
      }
    }

    if (description.getMethodName() != null) {
      line.append(description.getMethodName());
    }

    System.out.println(line.toString());
  }

  private Object formatTime(int timeMillis) {
    final int precision;
    if (timeMillis >= 100 * 1000) {
      precision = 0;
    } else if (timeMillis >= 10 * 1000) {
      precision = 1;
    } else {
      precision = 2;
    }
    return String.format(Locale.ENGLISH, "%4." + precision + "fs ", timeMillis / 1000.0);
  }
}
