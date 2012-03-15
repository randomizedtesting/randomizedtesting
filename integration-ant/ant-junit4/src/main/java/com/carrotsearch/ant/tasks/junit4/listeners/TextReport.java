package com.carrotsearch.ant.tasks.junit4.listeners;

import java.io.*;
import java.util.*;

import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.*;
import com.carrotsearch.ant.tasks.junit4.events.EventType;
import com.carrotsearch.ant.tasks.junit4.events.IEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.*;
import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

/**
 * A listener that will subscribe to test execution and dump
 * informational info about the progress to the console or a text
 * file.
 */
public class TextReport implements AggregatedEventListener {
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

  private boolean showStatusIgnored = true; 
  private boolean showStatusError = true;
  private boolean showStatusFailure = true;
  private boolean showStatusOk = true;

  /**
   * @see #setShowThrowable(boolean)
   */
  private boolean showThrowable = true;

  /** @see #setShowStackTraces(boolean) */
  private boolean showStackTraces = true; 

  /** @see #setShowOutputStream(boolean) */
  private boolean showOutputStream;

  /** @see #setShowErrorStream(boolean) */
  private boolean showErrorStream;

  /** @see #setShowSuiteSummary(boolean) */
  private boolean showSuiteSummary;
  
  /**
   * @see #showStatusError
   * @see #showStatusOk
   * @see #showStatusFailure
   * @see #showStatusIgnored
   */
  private EnumMap<TestStatus,Boolean> displayStatus = Maps.newEnumMap(TestStatus.class);

  /**
   * The owner task.
   */
  private JUnit4 task;

  /**
   * A {@link Writer} if external file is used.
   */
  private Writer output; 

  public void setShowStatusError(boolean showStatusError)     { this.showStatusError = showStatusError;   }
  public void setShowStatusFailure(boolean showStatusFailure) { this.showStatusFailure = showStatusFailure; }
  public void setShowStatusIgnored(boolean showStatusIgnored) { this.showStatusIgnored = showStatusIgnored; }
  public void setShowStatusOk(boolean showStatusOk)           { this.showStatusOk = showStatusOk;  }
  
  /**
   * If enabled, displays extended error information for tests that failed
   * (exception class, message, stack trace, standard streams).
   * 
   * @see #setShowStackTraces(boolean)
   * @see #setShowOutputStream(boolean)
   * @see #setShowErrorStream(boolean)
   */
  public void setShowThrowable(boolean showThrowable) {
    this.showThrowable = showThrowable;
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

  /**
   * If enabled, shows suite summaries in "maven-like" format of:
   * <pre>
   * Running SuiteName
   * [...suite tests if enabled...]
   * Tests: xx, Failures: xx, Errors: xx, Skipped: xx, Time: xx sec [<<< FAILURES!]
   * </pre>
   */
  public void setShowSuiteSummary(boolean showSuiteSummary) {
    this.showSuiteSummary = showSuiteSummary;
  }

  /**
   * Set an external file to write to.
   */
  public void setFile(File outputFile) throws IOException {
    if (!outputFile.getName().isEmpty()) {
      Files.createParentDirs(outputFile);
      this.output = Files.newWriter(outputFile, Charsets.UTF_8);
    }
  }

  /*
   * 
   */
  @Subscribe
  public void onTestResult(AggregatedTestResultEvent e) {
    // If we're aggregating over suites, wait.
    if (!showSuiteSummary) {
      format(e, e.getStatus(), e.getExecutionTime());
    }
  }

  /*
   * 
   */
  @Subscribe
  public void onSuiteResult(AggregatedSuiteResultEvent e) {
    if (showSuiteSummary) {
      log("Running " + e.getDescription().getDisplayName());

      // Static context output.
      if (shouldShowSuiteLevelOutput(e)) {
        String decoded = decodeStreamEvents(
            e.getSlave(), eventsBeforeFirstTest(e.getEventStream())).toString();
        if (!decoded.isEmpty()) {
          log(indent + "(@BeforeClass output)");
          log(decoded);
        }
      }

      // Tests.
      for (AggregatedTestResultEvent test : e.getTests()) {
        format(test, test.getStatus(), test.getExecutionTime());
      }

      // Trailing static context output.
      if (shouldShowSuiteLevelOutput(e)) {
        String decoded = decodeStreamEvents(
            e.getSlave(), eventsAfterLastTest(e.getEventStream())).toString();
        if (!decoded.isEmpty()) {
          log(indent + "(@AfterClass output)");
          log(decoded);
        }
      }
    }

    if (!e.getFailures().isEmpty()) {
      format(e, TestStatus.ERROR, 0);
    }

    if (showSuiteSummary) {
      log(
          String.format("Tests run: %3d, Failures: %3d, Errors: %3d, Skipped: %3d, Time: %5.2fs%s\n ",
              e.getTests().size(),
              e.getFailureCount(),
              e.getErrorCount(),
              e.getIgnoredCount(),
              e.getExecutionTime() / 1000.0d,
              e.isSuccessful() ? "" : " <<< FAILURES!"));
    }
  }

  /**
   * Display suite level output if showing the OK status or if the test wasn't successful. 
   */
  private boolean shouldShowSuiteLevelOutput(AggregatedSuiteResultEvent e) {
    return (showOutputStream || showErrorStream) &&
           (showStatusOk || !e.isSuccessful());
  }

  /**
   * Pick events before the first test starts (static context hooks).
   */
  private List<IEvent> eventsBeforeFirstTest(List<IEvent> eventStream) {
    int i = 0;
    for (IEvent event : eventStream) {
      if (event.getType() == EventType.TEST_STARTED) {
        return eventStream.subList(0, i);
      }
      i++;
    }

    // No test was ever started? Take the entire event stream. 
    return eventStream;
  }

  /**
   * Pick events after the last test ends (static context hooks).
   */
  private List<IEvent> eventsAfterLastTest(List<IEvent> eventStream) {
    if (!(eventStream instanceof RandomAccess)) {
      throw new RuntimeException("Event stream should be a RandomAccess list.");
    }

    for (int i = eventStream.size(); --i >= 0;) {
      // There should ALWAYS be a TEST_FINISHED event, even after unsuccessful tests.
      if (eventStream.get(i).getType() == EventType.TEST_FINISHED) {
        return eventStream.subList(i + 1, eventStream.size());
      }
    }

    // No test was ever finished? Weird.
    return Collections.emptyList();
  }

  @Subscribe
  public void onStart(AggregatedStartEvent e) {
    log("Executing " +
        e.getSuiteCount() + Pluralize.pluralize(e.getSuiteCount(), " suite") +
        " with " + 
        e.getSlaveCount() + Pluralize.pluralize(e.getSlaveCount(), " JVM") + ".");
  }

  /**
   * Log a message to the output.
   */
  private void log(String message) {
    if (output == null) {
      task.log(message);
    } else {
      try {
        output.write(message);
        output.write("\n");
        // Flush early for tailing etc.
        output.flush();
      } catch (IOException e) {
        // Ignore, what to do.
      }
    }
  }

  /*
   * 
   */
  private void format(AggregatedResultEvent result, TestStatus status, int timeMillis) {
    isStatusShown(TestStatus.ERROR);
    if (!isStatusShown(status)) {
      return;
    }

    SlaveInfo slave = result.getSlave();
    Description description = result.getDescription();
    List<FailureMirror> failures = result.getFailures();

    StringBuilder line = new StringBuilder();
    line.append(Strings.padEnd(statusNames.get(status), 8, ' '));
    line.append(formatTime(timeMillis));
    if (slave.slaves > 1) {
      final int digits = 1 + (int) Math.floor(Math.log10(slave.slaves));
      line.append(String.format(" J%-" + digits + "d", slave.id));
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

    if (showThrowable && !failures.isEmpty()) {
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
      CharSequence out = decodeStreamEvents(slave, result.getEventStream());
      if (out.length() > 0) {
        line.append(out);
        line.append("\n");
      }
    }

    log(line.toString().trim());
  }

  /**
   * Decode stream events, indent, format. 
   */
  private CharSequence decodeStreamEvents(SlaveInfo slave, List<IEvent> eventStream) {
    StringWriter sw = new StringWriter();
    Writer stdout = new PrefixedWriter(stdoutIndent, new LineBufferWriter(sw));
    Writer stderr = new PrefixedWriter(stderrIndent, new LineBufferWriter(sw));
    slave.decodeStreams(eventStream, stdout, stderr);
    return sw.getBuffer();
  }

  @Subscribe
  public void onQuit(AggregatedQuitEvent e) {
    if (output != null) {
      Closeables.closeQuietly(output);
    }
  }

  /*
   * 
   */
  private boolean isStatusShown(TestStatus status) {
    return displayStatus.get(status);
  }

  /*
   * 
   */
  private static Object formatTime(int timeMillis) {
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

    this.displayStatus.put(TestStatus.ERROR, showStatusError);
    this.displayStatus.put(TestStatus.FAILURE, showStatusFailure);
    this.displayStatus.put(TestStatus.IGNORED, showStatusIgnored);
    this.displayStatus.put(TestStatus.IGNORED_ASSUMPTION, showStatusIgnored);
    this.displayStatus.put(TestStatus.OK, showStatusOk);
  }
}
