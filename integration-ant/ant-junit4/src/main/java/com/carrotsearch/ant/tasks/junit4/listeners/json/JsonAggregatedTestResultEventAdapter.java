package com.carrotsearch.ant.tasks.junit4.listeners.json;

import java.lang.reflect.Type;
import java.util.Date;

import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedTestResultEvent;
import com.google.gson.*;

/**
 * Serialization of {@link AggregatedTestResultEvent}. 
 */
public class JsonAggregatedTestResultEventAdapter implements JsonSerializer<AggregatedTestResultEvent> {
  @Override
  public JsonElement serialize(AggregatedTestResultEvent e, 
      Type type, JsonSerializationContext context) {
    JsonObject suite = new JsonObject();
    suite.addProperty("slave", e.getSlave().id);
    suite.addProperty("startTimestamp", e.getStartTimestamp());
    suite.add("startTimestampDate", context.serialize(new Date(e.getStartTimestamp())));
    suite.addProperty("executionTime", e.getExecutionTime());
    suite.add("description", context.serialize(e.getDescription()));
    suite.addProperty("status", e.getStatus().name());

    suite.add("testFailures", context.serialize(e.getFailures()));
    return suite;
  }
}
