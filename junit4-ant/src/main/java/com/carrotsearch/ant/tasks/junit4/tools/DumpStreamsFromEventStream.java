package com.carrotsearch.ant.tasks.junit4.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.carrotsearch.ant.tasks.junit4.events.EventType;
import com.carrotsearch.ant.tasks.junit4.events.IStreamEvent;
import com.carrotsearch.ant.tasks.junit4.events.Serializer;
import com.google.common.base.Charsets;
import com.google.common.io.Closer;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class DumpStreamsFromEventStream {
  public static void main(String[] args) throws Exception {
    File inputFile = new File(args[0]);

    Gson gson = Serializer.createGSon(DumpStreamsFromEventStream.class.getClassLoader());

    Closer closer = Closer.create();
    try {
      OutputStream sysout = new BufferedOutputStream(new FileOutputStream(new File(inputFile.getAbsolutePath() + ".sysout")));
      closer.register(sysout);

      OutputStream syserr = new BufferedOutputStream(new FileOutputStream(new File(inputFile.getAbsolutePath() + ".syserr")));
      closer.register(syserr);

      InputStream is = new BufferedInputStream(new FileInputStream(inputFile));
      closer.register(is);
      JsonReader input = new JsonReader(new InputStreamReader(is, Charsets.UTF_8));
      input.setLenient(true);

      JsonToken peek;
      while (true) {
        peek = input.peek();
        
        if (peek == JsonToken.END_DOCUMENT) {
          return;
        }
  
        input.beginArray();
        EventType type = EventType.valueOf(input.nextString());
        switch (type) {
          case APPEND_STDERR:
            ((IStreamEvent) gson.fromJson(input, type.eventClass)).copyTo(syserr);
            break;
  
          case APPEND_STDOUT:
            ((IStreamEvent) gson.fromJson(input, type.eventClass)).copyTo(sysout);
            break;
  
          default:
            input.skipValue();
        }
        input.endArray();
      }
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
  }
}
