package com.carrotsearch.ant.tasks.junit4.events;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * Event deserializer.
 */
public class Deserializer {
  private JsonReader input;
  private Gson gson;
  
  public Deserializer(InputStream is, ClassLoader refLoader) throws IOException {
    input = new JsonReader(new InputStreamReader(is, Charsets.UTF_8));
    input.setLenient(true);

    gson = Serializer.createGSon(refLoader);
  }

  public IEvent deserialize() throws IOException {
    JsonToken peek = input.peek();
    if (peek == JsonToken.END_ARRAY)
      return null;

    input.beginArray();
    EventType type = EventType.valueOf(input.nextString());
    IEvent event = gson.fromJson(input, type.eventClass);
    input.endArray();

    return event;
  }
}
