package com.carrotsearch.ant.tasks.junit4.listeners;

import static com.carrotsearch.ant.tasks.junit4.FormattingUtils.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.FormattingUtils;
import com.carrotsearch.ant.tasks.junit4.JUnit4;
import com.carrotsearch.ant.tasks.junit4.Pluralize;
import com.carrotsearch.ant.tasks.junit4.events.IEvent;
import com.carrotsearch.ant.tasks.junit4.events.IStreamEvent;
import com.carrotsearch.ant.tasks.junit4.events.SuiteStartedEvent;
import com.carrotsearch.ant.tasks.junit4.events.TestFinishedEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedQuitEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedResultEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedStartEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedSuiteResultEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedSuiteStartedEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedTestResultEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.ChildBootstrap;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.HeartBeatEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.JvmOutputEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.PartialOutputEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.TestStatus;
import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;
import com.carrotsearch.randomizedtesting.WriterOutputStream;
import com.carrotsearch.randomizedtesting.annotations.SuppressForbidden;
import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.CharSink;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;

/**
 * A listener that will subscribe to test execution and dump
 * informational info about the progress to the console or a text
 * file.
 */
@SuppressWarnings("resource")
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
    statusNames = new EnumMap<>(TestStatus.class);
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

  /** @see #showEmptySuites(boolean) */
  private boolean showEmptySuites = false;
  
  /**
   * Status display info.
   */
  private final EnumMap<TestStatus,Boolean> displayStatus;

  /**
   * Initialize {@link #displayStatus}.
   */
  {
    displayStatus = new EnumMap<>(TestStatus.class);
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
  
  /** Summarize the first N failures at the end. */
  private int showNumFailuresAtEnd = 3;
  
  /** A list of failed tests, if to be displayed at the end. */
  private List<Description> failedTests = new ArrayList<>();

  /** Stack trace filters. */
  private List<StackTraceFilter> stackFilters = new ArrayList<>();

  private int totalSuites;
  private AtomicInteger totalErrors = new AtomicInteger();
  private AtomicInteger suitesCompleted = new AtomicInteger();
  private String seed;

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
   * Filter stack traces from certain frames. 
   */
  public void addConfigured(StackTraceFilter sfilter) {
    this.stackFilters.add(sfilter);
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
      this.outputMode = OutputMode.valueOf(mode.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("showOutput accepts any of: "
          + Arrays.toString(OutputMode.values()) + ", value is not valid: " + mode);
    }
  }

  /**
   * Summarize N failures at the end of the report.
   */
  public void setShowNumFailures(int num) {
    this.showNumFailuresAtEnd = num;
  }

  /**
   * Display suites without any errors and with no tests (resulting from filtering
   * expressions, for example).
   */
  public void setShowEmptySuites(boolean showEmptySuites) {
    this.showEmptySuites = showEmptySuites;
  }
  
  /**
   * If enabled, shows suite summaries in "maven-like" format of:
   * <pre>
   * Running SuiteName
   * [...suite tests if enabled...]
   * Tests: xx, Failures: xx, Errors: xx, Skipped: xx, Time: xx sec [&lt;&lt;&lt; FAILURES!]
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
  @SuppressForbidden("legitimate sysstreams.")
  public void setOuter(JUnit4 task) {
    this.seed = task.getSeed();

    if (outputFile != null) {
      try {
        Files.createParentDirs(outputFile);
        final CharSink charSink;
        if (append) {
          charSink = Files.asCharSink(outputFile, Charsets.UTF_8, FileWriteMode.APPEND);
        } else {
          charSink = Files.asCharSink(outputFile, Charsets.UTF_8);
        }
        this.output = charSink.openBufferedStream();
      } catch (IOException e) {
        throw new BuildException(e);
      }
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
    }
  }

  /*
   * Test events subscriptions.
   */

  @Subscribe
  public void onStart(AggregatedStartEvent e) throws IOException {
    this.totalSuites = e.getSuiteCount();
    logShort("Executing " +
        totalSuites + Pluralize.pluralize(totalSuites, " suite") +
        " with " + 
        e.getSlaveCount() + Pluralize.pluralize(e.getSlaveCount(), " JVM") + ".\n", false);

    forkedJvmCount = e.getSlaveCount();
    jvmIdFormat = " J%-" + (1 + (int) Math.floor(Math.log10(forkedJvmCount))) + "d";

    outWriter = new PrefixedWriter(stdoutIndent, output, DEFAULT_MAX_LINE_WIDTH);
    errWriter = new PrefixedWriter(stderrIndent, output, DEFAULT_MAX_LINE_WIDTH);        
  }

  @Subscribe
  public void onChildBootstrap(ChildBootstrap e) throws IOException {
      logShort("Started J" + e.getSlave().id + " PID(" + e.getSlave().getPidString() + ").");
  }

  @Subscribe
  public void onHeartbeat(HeartBeatEvent e) throws IOException {
      logShort("HEARTBEAT J" + e.getSlave().id + " PID(" + e.getSlave().getPidString() + "): " +
          formatTime(e.getCurrentTime()) + ", stalled for " +
          formatDurationInSeconds(e.getNoEventDuration()) + " at: " +
          (e.getDescription() == null ? "<unknown>" : formatDescription(e.getDescription())));
  }

  @Subscribe
  public void onQuit(AggregatedQuitEvent e) throws IOException {
    if (showNumFailuresAtEnd > 0 && !failedTests.isEmpty()) {
      List<Description> sublist = this.failedTests; 
      StringBuilder b = new StringBuilder();
      b.append("\nTests with failures [seed: ").append(seed).append("]");
      if (sublist.size() > showNumFailuresAtEnd) {
        sublist = sublist.subList(0, showNumFailuresAtEnd);
        b.append(" (first " + showNumFailuresAtEnd + " out of " + failedTests.size() + ")");
      }
      b.append(":\n");
      for (Description description : sublist) {
        b.append("  - ").append(formatDescription(description, true)).append("\n");
      }
      b.append("\n");
      logShort(b, false);
    }

    if (output != null) {
      Closeables.close(output, true);
    }
  }

  @Subscribe
  public void onSuiteStart(AggregatedSuiteStartedEvent e) throws IOException {
    final Charset charset = e.getSlave().getCharset();
    outStream = new WriterOutputStream(outWriter, charset, DEFAULT_MAX_LINE_WIDTH, true);
    errStream = new WriterOutputStream(errWriter, charset, DEFAULT_MAX_LINE_WIDTH, true);

    if (showSuiteSummary && isPassthrough()) {
      SuiteStartedEvent evt = e.getSuiteStartedEvent();
      emitSuiteStart(evt.getDescription(), evt.getStartTimestamp());
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
        default:
          break;
      }
    }
  }

  @Subscribe
  public void onJvmOutput(JvmOutputEvent e) throws IOException {
    final String id = Integer.toString(e.getSlave().id);
    output.append(">>> JVM J").append(id)
          .append(" emitted unexpected output (verbatim) ----\n");

    try (Reader r = Files.newReader(e.getJvmOutputFile(), e.getSlave().getCharset())) {
      CharStreams.copy(r, output);
    }
    output.append("<<< JVM J" + id + ": EOF ----\n");    
  }

  @Subscribe
  public void onTestResult(AggregatedTestResultEvent e) throws IOException {
    if (isPassthrough() && displayStatus.get(e.getStatus())) {
      flushOutput();
      emitStatusLine(e, e.getStatus(), e.getExecutionTime());
    }

    if (!e.isSuccessful() && showNumFailuresAtEnd > 0) {
      failedTests.add(e.getDescription());
    }
  }

  @Subscribe
  public void onSuiteResult(AggregatedSuiteResultEvent e) throws IOException {
    final int completed = suitesCompleted.incrementAndGet();

    if (e.isSuccessful() && 
        e.getTests().isEmpty() && 
        !showEmptySuites) {
      return;
    }

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

    if (!e.getFailures().isEmpty() && showNumFailuresAtEnd > 0) {
      failedTests.add(e.getDescription());
    }

    // Emit suite summary line if requested.
    if (showSuiteSummary) {
      emitSuiteEnd(e, completed);
    }
  }

  private void emitBufferedEvents(AggregatedSuiteResultEvent e) throws IOException {
    final IdentityHashMap<TestFinishedEvent,AggregatedTestResultEvent> eventMap = new IdentityHashMap<>();
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
          
        default:
          break;          
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
          "Suite: " +
          FormattingUtils.padTo(maxClassNameColumns, suiteName, "[...]"));
  }

  /**
   * Suite end.
   */
  private void emitSuiteEnd(AggregatedSuiteResultEvent e, int suitesCompleted) throws IOException {
    assert showSuiteSummary;

    final StringBuilder b = new StringBuilder();
    final int totalErrors = this.totalErrors.addAndGet(e.isSuccessful() ? 0 : 1);
    b.append(String.format(Locale.ROOT, "%sCompleted [%d/%d%s]%s in %.2fs, ",
        shortTimestamp(e.getStartTimestamp() + e.getExecutionTime()),
        suitesCompleted,
        totalSuites,
        totalErrors == 0 ? "" : " (" + totalErrors + "!)",
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
    logShort(b, false);
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
      line.append(String.format(Locale.ROOT, jvmIdFormat, result.getSlave().id));
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
      }

      final List<FailureMirror> failures = result.getFailures();
      if (!failures.isEmpty()) {
        final StringWriter sw = new StringWriter();
        PrefixedWriter pos = new PrefixedWriter(indent, sw, DEFAULT_MAX_LINE_WIDTH);
        int count = 0;
        for (FailureMirror fm : failures) {
          count++;
            if (fm.isAssumptionViolation()) {
                pos.write(String.format(Locale.ROOT, 
                    "Assumption #%d: %s",
                    count, MoreObjects.firstNonNull(fm.getMessage(), "(no message)")));
            } else {
                pos.write(String.format(Locale.ROOT, 
                    "Throwable #%d: %s",
                    count,
                    showStackTraces ? filterStackTrace(fm.getTrace()) : fm.getThrowableString()));
            }
        }
        pos.completeLine();
        if (sw.getBuffer().length() > 0) {
          line.append(sw.toString());
        }
      }
    }

    logShort(line);
  }

  /**
   * Filter stack trace if {@link #addConfigured(StackTraceFilter)}.
   */
  private String filterStackTrace(String trace) {
    for (StackTraceFilter filter : stackFilters) {
      trace = filter.apply(trace);
    }
    return trace;
  }

  /**
   * Log a message line to the output.
   */
  private void logShort(CharSequence message, boolean trim) throws IOException {
    int length = message.length();
    if (trim) {
      while (length > 0 && Character.isWhitespace(message.charAt(length - 1))) {
        length--;
      }
    }

    char [] chars = new char [length + 1];
    for (int i = 0; i < length; i++) {
      chars[i] = message.charAt(i);
    }
    chars[length] = '\n';

    output.write(chars);
  }

  /**
   * logShort, trim whitespace.
   */
  private void logShort(CharSequence message) throws IOException {
    logShort(message, true);
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
