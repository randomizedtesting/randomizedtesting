package com.carrotsearch.ant.tasks.junit4.events;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonReader;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonToken;
import com.google.common.base.Charsets;

/**
 * Event deserializer.
 */
public class Deserializer {
  private JsonReader input;

  public Deserializer(InputStream is, ClassLoader refLoader) throws IOException {
    input = new JsonReader(new InputStreamReader(is, Charsets.UTF_8));
    input.setLenient(true);
  }

  public IEvent deserialize() throws IOException {
    JsonToken peek = input.peek();
    if (peek == JsonToken.END_ARRAY)
      return null;

    input.beginArray();
    EventType type = EventType.valueOf(input.nextString());
    IEvent event = type.deserialize(input);
    input.endArray();
    return event;
  }
}
