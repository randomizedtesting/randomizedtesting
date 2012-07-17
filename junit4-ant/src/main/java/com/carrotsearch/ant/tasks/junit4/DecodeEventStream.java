package com.carrotsearch.ant.tasks.junit4;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import com.carrotsearch.ant.tasks.junit4.events.EventType;
import com.carrotsearch.ant.tasks.junit4.events.IStreamEvent;
import com.carrotsearch.ant.tasks.junit4.events.json.JsonByteArrayAdapter;
import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

/**
 * A simple utility to decode JSON event stream's APPEND_* chunks back into
 * a character stream.
 */
public class DecodeEventStream {
  public static void main(String[] args) throws IOException {
    Reader is = new InputStreamReader(new FileInputStream(args[0]), Charsets.UTF_8);

    JsonReader reader = new JsonReader(is);
    reader.setLenient(true);

    Gson gson = new GsonBuilder()
      .registerTypeAdapter(byte[].class, new JsonByteArrayAdapter())
      .create();
    
    while (true) {
      reader.beginArray();
      EventType type = EventType.valueOf(reader.nextString());
      IStreamEvent evt;
      switch (type) {
        case APPEND_STDERR:
        case APPEND_STDOUT:
          evt = (IStreamEvent) gson.fromJson(reader, type.eventClass);
          System.out.write(evt.getChunk());
          break;
        default:
          System.out.println("\n\n## " + type);
          reader.skipValue();
      }
      reader.endArray();
    }
  }
}
