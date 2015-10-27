package com.carrotsearch.ant.tasks.junit4.events;

import java.io.IOException;

import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonReader;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonWriter;

public interface RemoteEvent extends IEvent {
  void serialize(JsonWriter writer) throws IOException;
  void deserialize(JsonReader reader) throws IOException;
}
