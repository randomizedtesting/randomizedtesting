package com.carrotsearch.ant.tasks.junit4.events;

import java.io.IOException;

import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonReader;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonWriter;

/**
 * Marker that the slave is idle and awaiting more suite names.
 */
public class IdleEvent extends AbstractEvent {
  public IdleEvent() {
    super(EventType.IDLE);
  }

  @Override
  public void serialize(JsonWriter writer) throws IOException {
    writer.beginObject();
    writer.endObject();
  }

  @Override
  public void deserialize(JsonReader reader) throws IOException {
    reader.beginObject();
    reader.endObject();
  }
}
