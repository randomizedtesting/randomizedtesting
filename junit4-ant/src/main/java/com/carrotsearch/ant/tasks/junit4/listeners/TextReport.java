package com.carrotsearch.ant.tasks.junit4.listeners;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import org.apache.commons.io.output.WriterOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.*;
import com.carrotsearch.ant.tasks.junit4.events.IEvent;
import com.carrotsearch.ant.tasks.junit4.events.IStreamEvent;
import com.carrotsearch.ant.tasks.junit4.events.SuiteStartedEvent;
import com.carrotsearch.ant.tasks.junit4.events.TestFinishedEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.*;
import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;
import com.google.common.base.*;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

import static com.carrotsearch.ant.tasks.junit4.FormattingUtils.*;

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
   * Failure marker string.
   */
  private static final String FAILURE_MARKER = " <<<";
  private static final String FAILURE_STRING = FAILURE_MARKER + " FAILURES!";

  /**
   * Default 16kb for maximum line width buffer. Otherwise we may get OOMs buffering
   * each line.
   */
  private static final int DEFAULT_MAX_LINE_WIDTH = 1024 * 16;

  /**
   * Code pages which are capable of displaying all unicode glyphs. 
   */
  private static Set<String> UNICODE_ENCODINGS = new HashSet<String>(Arrays.asList(
      "UTF-8", "UTF-16LE", "UTF-16", "UTF-16BE", "UTF-32"));

  /**
   * Display mode for output streams.
   */
  public static enum OutputMode {
    /** Always display the output emitted from tests. */
    ALWAYS,
    /** 
     * Display the output only if a test/ suite failed. This requires internal buffering
     * so the output will be shown only after a test completes. 
     */
    ONERROR,
    /**
     * Don't display the output, even on test failures.
     */
    NEVER;
  }

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

  /** @see #setShowThrowable(boolean) */
  private boolean showThrowable = true;

  /** @see #setShowStackTraces(boolean) */
  private boolean showStackTraces = true;

  /** @see #setShowOutput(String) */
  private OutputMode outputMode = OutputMode.ALWAYS; 

  /** @see #setShowSuiteSummary(boolean) */
  private boolean showSuiteSummary = true;

  /**
   * Status display info.
   */
  private final EnumMap<TestStatus,Boolean> displayStatus;

  /**
   * Initialize {@link #displayStatus}.
   */
  {
    displayStatus = Maps.newEnumMap(TestStatus.class);
    for (TestStatus s : TestStatus.values()) {
      displayStatus.put(s, true);
    }
  }

  /**
   * A {@link Writer} for writing output messages.
   */
  private Writer output; 

  /**
   * Maximum number of columns for class name.
   */
  private int maxClassNameColumns = Integer.MAX_VALUE;
  
  /**
   * Use simple names for suite names.
   */
  private boolean useSimpleNames = false;

  /**
   * Display timestamps and durations for tests/ suites.
   */
  private boolean timestamps = false;

  /**
   * {@link #output} file name.
   */
  private File outputFile;

  /**
   * Append to {@link #outputFile} if specified.
   */
  private boolean append;

  /**
   * Monitor used for coordinating outputs. 
   */
  private Object streamLock;

  /**
   * Forked concurrent JVM count. 
   */
  private int forkedJvmCount;
  
  /**
   * Format line for JVM ID string.
   */
  private String jvmIdFormat;
  
  /** Standard output, prefixed and decoded. */
  private PrefixedWriter outWriter;

  /** Standard error, prefixed and decoded. */
  private PrefixedWriter errWriter;
  
  /** sysout recode stream. */
  private WriterOutputStream outStream;

  /** syserr recode stream. */
  private WriterOutputStream errStream;

  public void setShowStatusError(boolean showStatus)   { displayStatus.put(TestStatus.ERROR, showStatus); }
  public void setShowStatusFailure(boolean showStatus) { displayStatus.put(TestStatus.FAILURE, showStatus); }
  public void setShowStatusOk(boolean showStatus)      { displayStatus.put(TestStatus.OK, showStatus);  }
  public void setShowStatusIgnored(boolean showStatus) { 
    displayStatus.put(TestStatus.IGNORED, showStatus);
    displayStatus.put(TestStatus.IGNORED_ASSUMPTION, showStatus);
  }

  /**
   * Set maximum number of class name columns before truncated with ellipsis.
   */
  public void setMaxClassNameColumns(int maxClassNameColumns) {
    this.maxClassNameColumns = maxClassNameColumns;
  }
  
  /**
   * Use simple class names for suite naming. 
   */
  public void setUseSimpleNames(boolean useSimpleNames) {
    this.useSimpleNames = useSimpleNames;
  }
  
  /**
   * Show duration timestamps for tests and suites.
   */
  public void setTimestamps(boolean timestamps) {
    this.timestamps = timestamps;
  }

  /**
   * If enabled, displays extended error information for tests that failed
   * (exception class, message, stack trace, standard streams).
   * 
   * @see #setShowStackTraces(boolean)
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
   * Display mode for output streams.
   */
  public void setShowOutput(String mode) {
    try {
      this.outputMode = OutputMode.valueOf(mode.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("showOutput accepts any of: "
          + Arrays.toString(OutputMode.values()) + ", value is not valid: " + mode);
    }
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
   * Set an external file to write to. That file will always be in UTF-8.
   */
  public void setFile(File outputFile) throws IOException {
    if (!outputFile.getName().isEmpty()) {
      this.outputFile = outputFile;
    }
  }

  /**
   * Append if {@link #setFile(File)} is also specified. 
   */
  public void setAppend(boolean append) {
    this.append = append;
  }

  /**
   * Initialization by container task {@link JUnit4}.
   */
  @Override
  public void setOuter(JUnit4 task) {
    if (outputFile != null) {
      try {
        Files.createParentDirs(outputFile);
        this.output = Files.newWriterSupplier(outputFile, Charsets.UTF_8, append).getOutput();
      } catch (IOException e) {
        throw new BuildException(e);
      }
      streamLock = new Object();
    } else {
      if (!UNICODE_ENCODINGS.contains(Charset.defaultCharset().name())) {
        task.log("Your default console's encoding may not display certain" +
            " unicode glyphs: " + Charset.defaultCharset().name(), 
            Project.MSG_INFO);
      }
      output = new LineFlushingWriter(new OutputStreamWriter(System.out, Charset.defaultCharset())) {
        @Override
        // Don't close the underlying stream, just flush.
        public void close() throws IOException {
          flush();
        }
      };
      streamLock = System.out;
    }
  }

  /*
   * Test events subscriptions.
   */

  @Subscribe
  public void onStart(AggregatedStartEvent e) throws IOException {
    synchronized (streamLock) {
      logShort("Executing " +
          e.getSuiteCount() + Pluralize.pluralize(e.getSuiteCount(), " suite") +
          " with " + 
          e.getSlaveCount() + Pluralize.pluralize(e.getSlaveCount(), " JVM") + ".");
    }

    forkedJvmCount = e.getSlaveCount();
    jvmIdFormat = " J%-" + (1 + (int) Math.floor(Math.log10(forkedJvmCount))) + "d";
    
    outWriter = new PrefixedWriter(stdoutIndent, output, DEFAULT_MAX_LINE_WIDTH);
    errWriter = new PrefixedWriter(stderrIndent, output, DEFAULT_MAX_LINE_WIDTH);        
  }

  @Subscribe
  public void onHeartbeat(HeartBeatEvent e) throws IOException {
    synchronized (streamLock) {
      String msg = "HEARTBEAT J" + e.getSlave().id + ": " +
              formatTime(e.getCurrentTime()) + ", no events in: " +
              formatDurationInSeconds(e.getNoEventDuration()) + ", approx. at: " +
              (e.getDescription() == null ? "<unknown>" : formatDescription(e.getDescription()));
      logShort(msg);
    }
  }

  @Subscribe
  public void onQuit(AggregatedQuitEvent e) {
    if (output != null) {
      Closeables.closeQuietly(output);
    }
  }

  @Subscribe
  public void onSuiteStart(AggregatedSuiteStartedEvent e) throws IOException {
    final Charset charset = e.getSlave().getCharset();
    outStream = new WriterOutputStream(outWriter, charset, DEFAULT_MAX_LINE_WIDTH, true);
    errStream = new WriterOutputStream(errWriter, charset, DEFAULT_MAX_LINE_WIDTH, true);

    if (showSuiteSummary && isPassthrough()) {
      synchronized (streamLock) {
        SuiteStartedEvent evt = e.getSuiteStartedEvent();
        emitSuiteStart(evt.getDescription(), evt.getStartTimestamp());
      }
    }
  }

  @Subscribe
  public void onOutput(PartialOutputEvent e) throws IOException {
    if (isPassthrough()) {
      // We only allow passthrough output if there is one JVM.
      switch (e.getEvent().getType()) {
        case APPEND_STDERR:
          ((IStreamEvent) e.getEvent()).copyTo(errStream);
          break;
        case APPEND_STDOUT:
          ((IStreamEvent) e.getEvent()).copyTo(outStream);
          break;
      }
    }
  }

  @Subscribe
  public void onTestResult(AggregatedTestResultEvent e) throws IOException {
    if (isPassthrough() && displayStatus.get(e.getStatus())) {
      synchronized (streamLock) {
        flushOutput();
        emitStatusLine(e, e.getStatus(), e.getExecutionTime());
      }
    }
  }

  @Subscribe
  public void onSuiteResult(AggregatedSuiteResultEvent e) throws IOException {
    synchronized (streamLock) {
      // We must emit buffered test and stream events (in case of failures).
      if (!isPassthrough()) {
        if (showSuiteSummary) {
          emitSuiteStart(e.getDescription(), e.getStartTimestamp());
        }
        emitBufferedEvents(e);
      }

      // Emit a synthetic failure for suite-level errors, if any.
      if (!e.getFailures().isEmpty() && displayStatus.get(TestStatus.ERROR)) {
        emitStatusLine(e, TestStatus.ERROR, 0);
      }

      // Emit suite summary line if requested.
      if (showSuiteSummary) {
        emitSuiteEnd(e);
      }
    }
  }
  private void emitBufferedEvents(AggregatedSuiteResultEvent e) throws IOException {
    final IdentityHashMap<TestFinishedEvent,AggregatedTestResultEvent> eventMap = Maps.newIdentityHashMap();
    for (AggregatedTestResultEvent tre : e.getTests()) {
      eventMap.put(tre.getTestFinishedEvent(), tre);
    }

    final boolean emitOutput = 
           (outputMode != OutputMode.NEVER) && 
           ((outputMode == OutputMode.ALWAYS && !isPassthrough()) || 
            (outputMode == OutputMode.ONERROR && !e.isSuccessful()));

    for (IEvent event : e.getEventStream()) {
      switch (event.getType()) {
        case APPEND_STDOUT:
          if (emitOutput) ((IStreamEvent) event).copyTo(outStream);
          break;

        case APPEND_STDERR:
          if (emitOutput) ((IStreamEvent) event).copyTo(errStream);
          break;

        case TEST_FINISHED:
          assert eventMap.containsKey(event);
          final AggregatedTestResultEvent aggregated = eventMap.get(event);
          if (displayStatus.get(aggregated.getStatus())) {
            flushOutput();
            emitStatusLine(aggregated, aggregated.getStatus(), aggregated.getExecutionTime());
          }
      }
    }

    if (emitOutput) {
      flushOutput();
    }
  }

  /**
   * Flush output streams.
   */
  private void flushOutput() throws IOException {
    outStream.flush();
    outWriter.completeLine();
    errStream.flush();
    errWriter.completeLine();
  }

  /**
   * Suite prologue.
   */
  private void emitSuiteStart(Description description, long startTimestamp) throws IOException {
    String suiteName = description.getDisplayName();
    if (useSimpleNames) {
      if (suiteName.lastIndexOf('.') >= 0) {
        suiteName = suiteName.substring(suiteName.lastIndexOf('.') + 1);
      }
    }
    logShort(shortTimestamp(startTimestamp) +
        "Suite: " + FormattingUtils.padTo(maxClassNameColumns, suiteName, "[...]"));
  }

  /**
   * Suite end.
   */
  private void emitSuiteEnd(AggregatedSuiteResultEvent e) throws IOException {
    assert showSuiteSummary;

    final StringBuilder b = new StringBuilder();
    b.append(String.format(Locale.ENGLISH, "%sCompleted%s in %.2fs, ",
        shortTimestamp(e.getStartTimestamp() + e.getExecutionTime()),
        e.getSlave().slaves > 1 ? " on J" + e.getSlave().id : "",
        e.getExecutionTime() / 1000.0d));
    b.append(e.getTests().size()).append(Pluralize.pluralize(e.getTests().size(), " test"));

    int failures = e.getFailureCount();
    if (failures > 0) {
      b.append(", ").append(failures).append(Pluralize.pluralize(failures, " failure"));
    }

    int errors = e.getErrorCount();
    if (errors > 0) {
      b.append(", ").append(errors).append(Pluralize.pluralize(errors, " error"));
    }

    int ignored = e.getIgnoredCount();
    if (ignored > 0) {
      b.append(", ").append(ignored).append(" skipped");
    }

    if (!e.isSuccessful()) {
      b.append(FAILURE_STRING);
    }

    b.append("\n"); 
    logShort(b.toString());
  }

  /**
   * Emit status line for an aggregated event.
   */
  private void emitStatusLine(AggregatedResultEvent result, TestStatus status, long timeMillis) throws IOException {
    final StringBuilder line = new StringBuilder();

    line.append(shortTimestamp(result.getStartTimestamp()));
    line.append(Strings.padEnd(statusNames.get(status), 8, ' '));
    line.append(formatDurationInSeconds(timeMillis));
    if (forkedJvmCount > 1) {
      line.append(String.format(jvmIdFormat, result.getSlave().id));
    }
    line.append(" | ");

    line.append(formatDescription(result.getDescription()));
    if (!result.isSuccessful()) {
      line.append(FAILURE_MARKER);
    }
    line.append("\n");

    if (showThrowable) {
      // GH-82 (cause for ignored tests). 
      if (status == TestStatus.IGNORED && result instanceof AggregatedTestResultEvent) {
        final StringWriter sw = new StringWriter();
        PrefixedWriter pos = new PrefixedWriter(indent, sw, DEFAULT_MAX_LINE_WIDTH);
        pos.write("Cause: ");
        pos.write(((AggregatedTestResultEvent) result).getCauseForIgnored());
        pos.completeLine();
        line.append(sw.toString());
        line.append("\n");
      }

      final List<FailureMirror> failures = result.getFailures();
      if (!failures.isEmpty()) {
        final StringWriter sw = new StringWriter();
        PrefixedWriter pos = new PrefixedWriter(indent, sw, DEFAULT_MAX_LINE_WIDTH);
        int count = 0;
        for (FailureMirror fm : failures) {
          count++;
            if (fm.isAssumptionViolation()) {
                pos.write(String.format(Locale.ENGLISH, 
                    "Assumption #%d: %s",
                    count, com.google.common.base.Objects.firstNonNull(fm.getMessage(), "(no message)")));
            } else {
                pos.write(String.format(Locale.ENGLISH, 
                    "Throwable #%d: %s",
                    count,
                    showStackTraces ? fm.getTrace() : fm.getThrowableString()));
            }
        }
        pos.completeLine();
        if (sw.getBuffer().length() > 0) {
          line.append(sw.toString());
          line.append("\n");
        }
      }
    }

    logShort(line.toString().trim());
  }

  /**
   * Log a message line to the output.
   */
  private void logShort(String message) throws IOException {
    assert Thread.holdsLock(streamLock);
    output.write(message);
    output.write("\n");
  }

  /**
   * @return <code>true</code> if we can emit output directly and immediately.
   */
  private boolean isPassthrough() {
    return forkedJvmCount == 1 && outputMode == OutputMode.ALWAYS;
  }

  /**
   * Format a short timestamp.
   */
  private String shortTimestamp(long ts) {
    if (timestamps) {
      return "[" + formatTimestamp(ts) + "] ";
    } else {
      return "";
    }
  }
}
