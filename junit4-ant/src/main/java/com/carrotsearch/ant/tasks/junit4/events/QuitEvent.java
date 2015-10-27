package com.carrotsearch.ant.tasks.junit4.events;

import java.io.IOException;

import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonReader;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonWriter;

/**
 * Final message sent from the slave. Also signals orderly shutdown.
 */
public class QuitEvent extends AbstractEvent {
  public QuitEvent() {
    super(EventType.QUIT);
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
