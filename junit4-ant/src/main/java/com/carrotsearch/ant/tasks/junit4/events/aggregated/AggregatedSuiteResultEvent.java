package com.carrotsearch.ant.tasks.junit4.events.aggregated;

import static com.carrotsearch.ant.tasks.junit4.events.EventType.*;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.ForkedJvmInfo;
import com.carrotsearch.ant.tasks.junit4.events.EventType;
import com.carrotsearch.ant.tasks.junit4.events.FailureEvent;
import com.carrotsearch.ant.tasks.junit4.events.IDescribable;
import com.carrotsearch.ant.tasks.junit4.events.IEvent;
import com.carrotsearch.ant.tasks.junit4.events.IStreamEvent;
import com.carrotsearch.ant.tasks.junit4.events.JsonHelpers;
import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonWriter;
import com.carrotsearch.randomizedtesting.WriterOutputStream;

public class AggregatedSuiteResultEvent implements AggregatedResultEvent {
  private transient final ForkedJvmInfo slave;

  private final long executionTime;
  private final long startTimestamp;
  private final Description description;

  private final List<AggregatedTestResultEvent> tests;
  private final List<FailureMirror> suiteFailures;
  private final List<IEvent> eventStream;

  private final AggregatedSuiteStartedEvent startEvent;

  public AggregatedSuiteResultEvent(
      AggregatedSuiteStartedEvent startEvent,
      ForkedJvmInfo id, 
      Description description, 
      List<FailureMirror> suiteFailures, 
      List<AggregatedTestResultEvent> tests,
      List<IEvent> eventStream,
      long startTimestamp, 
      long executionTime) {
    this.startEvent = startEvent;
    this.slave = id;
    this.tests = tests;
    this.suiteFailures = suiteFailures;
    this.description = description;
    this.eventStream = eventStream;
    this.executionTime = executionTime;
    this.startTimestamp = startTimestamp;
  }

  public AggregatedSuiteStartedEvent getStartEvent() {
    return startEvent;
  }
  
  public List<AggregatedTestResultEvent> getTests() {
    return tests;
  }

  @Override
  public List<FailureMirror> getFailures() {
    return Collections.unmodifiableList(suiteFailures);
  }

  @Override
  public boolean isSuccessful() {
    if (!suiteFailures.isEmpty())
      return false;

    for (AggregatedTestResultEvent e : tests) {
      if (!e.isSuccessful()) {
        return false;
      }
    }

    return true;
  }

  @Override
  public List<IEvent> getEventStream() {
    return eventStream;
  }
  
  @Override
  public ForkedJvmInfo getSlave() {
    return slave;
  }

  @Override
  public Description getDescription() {
    return description;
  }

  /**
   * Execution time in milliseconds.
   */
  public long getExecutionTime() {
    return executionTime;
  }

  /**
   * Execution start timestamp (on the slave).
   */
  public long getStartTimestamp() {
    return startTimestamp;
  }

  /**
   * The number of tests that have {@link TestStatus#FAILURE} and
   * include assertion violations at suite level.
   */
  public int getFailureCount() {
    int count = 0;
    for (AggregatedTestResultEvent t : getTests()) {
      if (t.getStatus() == TestStatus.FAILURE)
        count++;
    }
    for (FailureMirror m : getFailures()) {
      if (m.isAssertionViolation())
        count++;
    }
    return count;
  }
  
  /**
   * The number of tests that have {@link TestStatus#ERROR} and
   * include the suite-level errors.
   */
  public int getErrorCount() {
    int count = 0;
    for (AggregatedTestResultEvent t : getTests()) {
      if (t.getStatus() == TestStatus.ERROR)
        count++;
    }
    
    for (FailureMirror m : getFailures()) {
      if (m.isErrorViolation())
        count++;
    }
    return count;
  }

  /**
   * Return the number of ignored or assumption-ignored tests.
   */
  public int getIgnoredCount() {
    int count = 0;
    for (AggregatedTestResultEvent t : getTests()) {
      if (t.getStatus() == TestStatus.IGNORED ||
          t.getStatus() == TestStatus.IGNORED_ASSUMPTION) {
        count++;
      }
    }
    return count;
  }

  public void serialize(JsonWriter w, boolean outputStreams) throws IOException {
    w.beginObject();

    w.name("slave").value(getSlave().id);
    w.name("startTimestamp").value(getStartTimestamp());

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ROOT);
    w.name("startTimestampDate").value(sdf.format(new Date(getStartTimestamp())));

    w.name("executionTime").value(getExecutionTime());
    w.name("description");
    JsonHelpers.writeDescription(w, getDescription());
    
    w.name("tests");
    w.beginArray();
    for (AggregatedTestResultEvent e : getTests()) {
      serialize(w, sdf, e);
    }
    w.endArray();

    w.name("suiteFailures");
    w.beginArray();
    for (FailureMirror m : getFailures()) {
      serialize(w, m);
    }
    w.endArray();

    w.name("executionEvents");
    w.beginArray();
    serializeEvents(w, outputStreams);
    w.endArray();

    w.endObject();
  }

  private void serialize(JsonWriter w, SimpleDateFormat sdf, AggregatedTestResultEvent e) throws IOException {
    w.beginObject();
    w.name("slave").value(e.getSlave().id);
    w.name("startTimestamp").value(e.getStartTimestamp());
    w.name("startTimestampDate").value(sdf.format(new Date(e.getStartTimestamp())));
    w.name("executionTime").value(e.getExecutionTime());
    w.name("description");
    JsonHelpers.writeDescription(w, e.getDescription());
    w.name("status").value(e.getStatus().name());

    w.name("testFailures");
    w.beginArray();
    for (FailureMirror m : e.getFailures()) {
      serialize(w, m);
    }
    w.endArray();

    w.endObject();
  }

  private void serialize(JsonWriter w, FailureMirror e) throws IOException {
    w.beginObject();
    w.name("throwableClass").value(e.getThrowableClass());
    w.name("throwableString").value(e.getThrowableString());
    w.name("stackTrace").value(e.getTrace());

    String throwableKind;
    if (e.isAssertionViolation()) { 
      throwableKind = "assertion";
    } else if (e.isErrorViolation()) {
      throwableKind = "error";
    } else if (e.isAssumptionViolation()) {
      throwableKind = "assumption";
    } else {
      throwableKind = "unknown";
    }
    w.name("kind").value(throwableKind);
    w.endObject();
  }

  private void serializeEvents(JsonWriter w, boolean outputStreams) throws IOException {
    final Charset charset = getSlave().getCharset();
    int lineBuffer = 160;
    final StringWriter out = new StringWriter();
    final StringWriter err = new StringWriter();
    WriterOutputStream stdout = new WriterOutputStream(out, charset, lineBuffer, false);
    WriterOutputStream stderr = new WriterOutputStream(err, charset, lineBuffer, false);
    for (IEvent evt : getEventStream()) {
      try {
        switch (evt.getType()) {
          case SUITE_FAILURE:
          case TEST_IGNORED_ASSUMPTION:
          case TEST_IGNORED:
          case TEST_STARTED:
          case TEST_FINISHED:
          case TEST_FAILURE:
            flushBoth(w, out, err, stdout, stderr);
            
            w.beginObject();
            w.name("event").value(evt.getType().toString());
            w.name("description");
            JsonHelpers.writeDescription(w, ((IDescribable) evt).getDescription());

            if (evt instanceof FailureEvent) {
              w.name("failure");
              ((FailureEvent) evt).serialize(w);
            }
            w.endObject();
            break;

          // Flush streams only if there's interwoven output between them.

          case APPEND_STDOUT:
            if (outputStreams) {
              flush(APPEND_STDERR, w, stderr, err);
              ((IStreamEvent) evt).copyTo(stdout);
            }
            break;

          case APPEND_STDERR:
            if (outputStreams) {
              flush(APPEND_STDOUT, w, stdout, out);
              ((IStreamEvent) evt).copyTo(stderr);
            }
            break;

          default:
            break;
        }
      } catch (IOException ex) {
        // Ignore.
      }
    }
    flushBoth(w, out, err, stdout, stderr);
  }
  
  public void flushBoth(JsonWriter w, StringWriter out, StringWriter err, WriterOutputStream stdout, WriterOutputStream stderr) throws IOException {
    flush(APPEND_STDOUT, w, stdout, out);
    flush(APPEND_STDERR, w, stderr, err);
  }

  private void flush(EventType evt, JsonWriter w, WriterOutputStream wos, StringWriter out) throws IOException {
    wos.flush();
    if (out.getBuffer().length() > 0) {
      w.beginObject();
      w.name("event").value(evt.toString());
      w.name("content").value(out.getBuffer().toString());
      out.getBuffer().setLength(0);
      w.endObject();
    }
  }  
}
