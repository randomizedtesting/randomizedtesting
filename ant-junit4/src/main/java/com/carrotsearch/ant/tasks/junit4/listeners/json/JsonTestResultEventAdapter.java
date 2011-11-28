package com.carrotsearch.ant.tasks.junit4.listeners.json;

import java.lang.reflect.Type;

import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedTestResultEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.TestStatus;
import com.google.gson.*;

/**
 * Serialization of {@link AggregatedTestResultEvent}. 
 */
public class JsonTestResultEventAdapter implements JsonSerializer<AggregatedTestResultEvent> {
  @Override
  public JsonElement serialize(AggregatedTestResultEvent e, 
      Type type, JsonSerializationContext context) {
    JsonObject suite = new JsonObject();
    suite.add("description", context.serialize(e.getDescription()));

    suite.addProperty("executionTime", e.getExecutionTime());
    suite.addProperty("slave", e.getSlave().id);
    suite.addProperty("status", e.getStatus().name());

    suite.addProperty("wasFailure", e.getStatus() == TestStatus.FAILURE);
    suite.addProperty("wasError", e.getStatus() == TestStatus.ERROR);
    suite.addProperty("wasIgnored",
        e.getStatus() == TestStatus.IGNORED ||
        e.getStatus() == TestStatus.IGNORED_ASSUMPTION);
    suite.addProperty("wasIgnoredByAssumption",
        e.getStatus() == TestStatus.IGNORED_ASSUMPTION);

    suite.add("failures", context.serialize(e.getFailures()));
    return suite;
  }
}
