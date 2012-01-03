package com.carrotsearch.ant.tasks.junit4.listeners.json;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;

import org.apache.commons.io.output.WriterOutputStream;

import com.carrotsearch.ant.tasks.junit4.SlaveInfo;
import com.carrotsearch.ant.tasks.junit4.events.*;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedSuiteResultEvent;
import com.google.gson.*;

/**
 * Serialization of {@link AggregatedSuiteResultEvent}.
 */
public class JsonSuiteResultEventAdapter implements JsonSerializer<AggregatedSuiteResultEvent> {
  @Override
  public JsonElement serialize(AggregatedSuiteResultEvent e, 
      Type type, JsonSerializationContext context) {
    JsonObject suite = new JsonObject();
    suite.addProperty("slave", e.getSlave().id);
    suite.addProperty("startTimestamp", e.getStartTimestamp());
    suite.addProperty("executionTime", e.getExecutionTime());

    suite.add("description", context.serialize(e.getDescription()));

    suite.add("tests", context.serialize(e.getTests()));
    suite.add("failures", context.serialize(e.getFailures()));
    suite.add("output", serializeOutputStreams(e));
    return suite;
  }

  public JsonArray serializeOutputStreams(AggregatedSuiteResultEvent e) {
    final JsonArray output = new JsonArray();
    final SlaveInfo slave = e.getSlave();
    int lineBuffer = 160;
    final StringWriter out = new StringWriter();
    final StringWriter err = new StringWriter();
    WriterOutputStream stdout = new WriterOutputStream(out, slave.getCharset(), lineBuffer, false);
    WriterOutputStream stderr = new WriterOutputStream(err, slave.getCharset(), lineBuffer, false);
    for (IEvent evt : e.getEventStream()) {
      try {
        switch (evt.getType()) {
          case TEST_STARTED:
          case TEST_FINISHED:
            flushBoth(output, out, err, stdout, stderr);
            JsonObject marker = new JsonObject();
            marker.addProperty("test", ((AbstractEventWithDescription) evt).getDescription().getDisplayName());
            marker.addProperty("event", evt.getType().toString());
            output.add(marker);
            break;

          case APPEND_STDOUT:
            flush("err", output, stderr, err);
            stdout.write(((AppendStdOutEvent) evt).getChunk());
            break;

          case APPEND_STDERR:
            flush("out", output, stdout, out);
            stderr.write(((AppendStdErrEvent) evt).getChunk());
            break;
        }
      } catch (IOException ex) {
        // Ignore.
      }
    }
    flushBoth(output, out, err, stdout, stderr);
    return output;
  }

  public void flushBoth(JsonArray output, final StringWriter out,
      final StringWriter err, WriterOutputStream stdout,
      WriterOutputStream stderr) {
    try {
      flush("out", output, stdout, out);
      flush("err", output, stderr, err);
    } catch (IOException ex) {
      // Ignore.
    }
  }

  private void flush(String streamName, JsonArray output, WriterOutputStream wos,
      StringWriter out) throws IOException {
    wos.flush();
    if (out.getBuffer().length() > 0) {
      JsonObject chunk = new JsonObject();
      chunk.addProperty(streamName, out.getBuffer().toString());
      out.getBuffer().setLength(0);
      output.add(chunk);
    }
  }
}
