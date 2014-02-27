package com.carrotsearch.ant.tasks.junit4.listeners.json;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.Date;

import org.apache.commons.io.output.WriterOutputStream;

import com.carrotsearch.ant.tasks.junit4.ForkedJvmInfo;
import com.carrotsearch.ant.tasks.junit4.events.*;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedSuiteResultEvent;
import com.google.gson.*;

import static com.carrotsearch.ant.tasks.junit4.events.EventType.*;

/**
 * Serialization of {@link AggregatedSuiteResultEvent}.
 */
public class JsonAggregatedSuiteResultEventAdapter implements JsonSerializer<AggregatedSuiteResultEvent> {
  private final boolean outputStreams;

  public JsonAggregatedSuiteResultEventAdapter(boolean outputStreams) {
    this.outputStreams = outputStreams;
  }

  @Override
  public JsonElement serialize(AggregatedSuiteResultEvent e, 
      Type type, JsonSerializationContext context) {
    JsonObject suite = new JsonObject();
    suite.addProperty("slave", e.getSlave().id);
    suite.addProperty("startTimestamp", e.getStartTimestamp());
    suite.add("startTimestampDate", context.serialize(new Date(e.getStartTimestamp())));
    suite.addProperty("executionTime", e.getExecutionTime());

    suite.add("description", context.serialize(e.getDescription()));

    suite.add("tests", context.serialize(e.getTests()));
    suite.add("suiteFailures", context.serialize(e.getFailures()));
    suite.add("executionEvents", serializeEvents(e, context));
    return suite;
  }

  public JsonArray serializeEvents(AggregatedSuiteResultEvent e, JsonSerializationContext context) {
    final JsonArray output = new JsonArray();
    final ForkedJvmInfo slave = e.getSlave();
    int lineBuffer = 160;
    final StringWriter out = new StringWriter();
    final StringWriter err = new StringWriter();
    WriterOutputStream stdout = new WriterOutputStream(out, slave.getCharset(), lineBuffer, false);
    WriterOutputStream stderr = new WriterOutputStream(err, slave.getCharset(), lineBuffer, false);
    for (IEvent evt : e.getEventStream()) {
      try {
        JsonObject marker;
        switch (evt.getType()) {
          case SUITE_FAILURE:
          case TEST_IGNORED_ASSUMPTION:
          case TEST_IGNORED:
          case TEST_STARTED:
          case TEST_FINISHED:
          case TEST_FAILURE:
            flushBoth(output, out, err, stdout, stderr);
            marker = new JsonObject();
            marker.addProperty("event", evt.getType().toString());
            marker.add("description", context.serialize(((IDescribable) evt).getDescription()));
            if (evt instanceof FailureEvent) {
              marker.add("failure", context.serialize(((FailureEvent) evt).getFailure()));
            }
            output.add(marker);
            break;

          // Flush streams only if there's interwoven output between them.

          case APPEND_STDOUT:
            if (outputStreams) {
              flush(APPEND_STDERR, output, stderr, err);
              ((IStreamEvent) evt).copyTo(stdout);
            }
            break;

          case APPEND_STDERR:
            if (outputStreams) {
              flush(APPEND_STDOUT, output, stdout, out);
              ((IStreamEvent) evt).copyTo(stderr);
            }
            break;
        }
      } catch (IOException ex) {
        // Ignore.
      }
    }
    flushBoth(output, out, err, stdout, stderr);
    return output;
  }

  public void flushBoth(JsonArray output, final StringWriter out, final StringWriter err, WriterOutputStream stdout, WriterOutputStream stderr) {
    try {
      flush(APPEND_STDOUT, output, stdout, out);
      flush(APPEND_STDERR, output, stderr, err);
    } catch (IOException ex) {
      // Ignore.
    }
  }

  private void flush(EventType evt, JsonArray output, WriterOutputStream wos, StringWriter out) throws IOException {
    wos.flush();
    if (out.getBuffer().length() > 0) {
      JsonObject chunk = new JsonObject();
      chunk.addProperty("event", evt.toString());
      chunk.addProperty("content", out.getBuffer().toString());
      out.getBuffer().setLength(0);
      output.add(chunk);
    }
  }
}
