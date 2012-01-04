package com.carrotsearch.ant.tasks.junit4.listeners.json;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.Date;

import org.apache.commons.io.output.WriterOutputStream;

import com.carrotsearch.ant.tasks.junit4.SlaveInfo;
import com.carrotsearch.ant.tasks.junit4.events.*;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedSuiteResultEvent;
import com.google.gson.*;

import static com.carrotsearch.ant.tasks.junit4.events.EventType.*;

/**
 * Serialization of {@link AggregatedSuiteResultEvent}.
 */
public class JsonAggregatedSuiteResultEventAdapter implements JsonSerializer<AggregatedSuiteResultEvent> {
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
    suite.add("executionEvents", serializeEvents(e));
    return suite;
  }

  public JsonArray serializeEvents(AggregatedSuiteResultEvent e) {
    final JsonArray output = new JsonArray();
    final SlaveInfo slave = e.getSlave();
    int lineBuffer = 160;
    final StringWriter out = new StringWriter();
    final StringWriter err = new StringWriter();
    WriterOutputStream stdout = new WriterOutputStream(out, slave.getCharset(), lineBuffer, false);
    WriterOutputStream stderr = new WriterOutputStream(err, slave.getCharset(), lineBuffer, false);
    for (IEvent evt : e.getEventStream()) {
      try {
        JsonObject marker;
        switch (evt.getType()) {
          case TEST_STARTED:
          case TEST_FINISHED:
            flushBoth(output, out, err, stdout, stderr);
            marker = new JsonObject();
            marker.addProperty("event", evt.getType().toString());
            marker.addProperty("test", ((AbstractEventWithDescription) evt).getDescription().getDisplayName());
            output.add(marker);
            break;
            
          // Flush streams only if there's interwoven output between them.

          case APPEND_STDOUT:
            flush(APPEND_STDERR, output, stderr, err);
            stdout.write(((IStreamEvent) evt).getChunk());
            break;

          case APPEND_STDERR:
            flush(APPEND_STDOUT, output, stdout, out);
            stderr.write(((IStreamEvent) evt).getChunk());
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
