package com.carrotsearch.ant.tasks.junit4.listeners;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

import org.apache.tools.ant.Project;
import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.JUnit4;
import com.carrotsearch.ant.tasks.junit4.Pluralize;
import com.carrotsearch.ant.tasks.junit4.SlaveInfo;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedResultEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedStartEvent;
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
  /*
   * Indents for outputs.
   */
  private static final String indent       = "   > ";
  private static final String stdoutIndent = "  1> ";
  private static final String stderrIndent = "  2> ";

  /**
   * Status names column.
   */
  private static EnumMap<TestStatus, String> statusNames;
  static {
    statusNames = Maps.newEnumMap(TestStatus.class);
    for (TestStatus s : TestStatus.values()) {
      statusNames.put(s,
          s == TestStatus.IGNORED_ASSUMPTION
          ? "IGNOR/A" : s.toString());
    }
  }

  /** @see #setShowErrors(boolean) */
  private boolean showErrors; 

  /** @see #setShowStackTraces(boolean) */
  private boolean showStackTraces; 

  /** @see #setShowOutputStream(boolean) */
  private boolean showOutputStream; 

  /** @see #setShowErrorStream(boolean) */
  private boolean showErrorStream;
  private JUnit4 task; 

  /**
   * Show error information.
   */
  public void setShowErrors(boolean showErrors) {
    this.showErrors = showErrors;
  }

  /**
   * Show stack trace information.
   */
  public void setShowStackTraces(boolean showStackTraces) {
    this.showStackTraces = showStackTraces;
  }
  
  /**
   * Show error stream from tests.
   */
  public void setShowErrorStream(boolean showErrorStream) {
    this.showErrorStream = showErrorStream;
  }

  /**
   * Show output stream from tests.
   */
  public void setShowOutputStream(boolean showOutputStream) {
    this.showOutputStream = showOutputStream;
  }

  /*
   * 
   */
  @Subscribe
  public void onTestResult(AggregatedTestResultEvent e) {
    format(e, e.getStatus(), e.getExecutionTime());
  }

  /*
   * 
   */
  @Subscribe
  public void onSuiteResult(AggregatedSuiteResultEvent e) {
    if (!e.getFailures().isEmpty()) {
      format(e, TestStatus.ERROR, 0);
    }
  }

  @Subscribe
  public void onStart(AggregatedStartEvent e) {
    task.log("Executing tests with " + 
        e.getSlaveCount() + Pluralize.pluralize(e.getSlaveCount(), " JVM") + ".", Project.MSG_INFO);
    task.log("JUnit4 random (shuffle order): " + task.getSeed());
  }
  
  /*
   * 
   */
  private void format(AggregatedResultEvent result, TestStatus status, int timeMillis) {
    SlaveInfo slave = result.getSlave();
    Description description = result.getDescription();
    List<FailureMirror> failures = result.getFailures();

    StringBuilder line = new StringBuilder();
    line.append(Strings.padEnd(statusNames.get(status), 8, ' '));
    line.append(formatTime(timeMillis));
    if (slave.slaves > 1) {
      final int digits = 1 + (int) Math.floor(Math.log10(slave.slaves));
      line.append(String.format(" S%-" + digits + "d", slave.id));
    }    
    line.append(" | ");

    String className = description.getClassName();
    if (className != null) {
      String [] components = className.split("[\\.]");
      className = components[components.length - 1];
      line.append(className);
      if (description.getMethodName() != null) { 
        line.append(".").append(description.getMethodName());
      } else {
        line.append(" (suite)");
      }
    } else {
      if (description.getMethodName() != null) {
        line.append(description.getMethodName());
      }
    }
    line.append("\n");

    if (showErrors && !failures.isEmpty()) {
      StringWriter sw = new StringWriter();
      PrefixedWriter pos = new PrefixedWriter(indent, sw);
      int count = 0;
      for (FailureMirror fm : failures) {
        count++;
        try {
          final String details = 
              (showStackTraces && !fm.isAssumptionViolation())
              ? fm.getTrace()
              : fm.getThrowableString();

          pos.write(String.format(Locale.ENGLISH, 
              "Throwable #%d: %s",
              count, details));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      if (sw.getBuffer().length() > 0) {
        line.append(sw.toString());
        line.append("\n");
      }
    }

    if (showOutputStream || showErrorStream) {
      StringWriter sw = new StringWriter();
      Writer stdout = new PrefixedWriter(stdoutIndent, new LineBufferWriter(sw));
      Writer stderr = new PrefixedWriter(stderrIndent, new LineBufferWriter(sw));
      slave.decodeStreams(result.getEventStream(), stdout, stderr);
      
      if (sw.getBuffer().length() > 0) {
        line.append(sw.toString());
        line.append("\n");
      }
    }

    task.log(line.toString().trim(), Project.MSG_INFO);
  }

  /*
   * 
   */
  private Object formatTime(int timeMillis) {
    final int precision;
    if (timeMillis >= 100 * 1000) {
      precision = 0;
    } else if (timeMillis >= 10 * 1000) {
      precision = 1;
    } else {
      precision = 2;
    }
    return String.format(Locale.ENGLISH, "%4." + precision + "fs", timeMillis / 1000.0);
  }

  @Override
  public void setOuter(JUnit4 junit) {
    this.task = junit;
  }
}
