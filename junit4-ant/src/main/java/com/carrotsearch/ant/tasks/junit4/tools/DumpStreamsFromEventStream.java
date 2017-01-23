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
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonReader;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonToken;
import com.carrotsearch.randomizedtesting.annotations.SuppressForbidden;
import com.google.common.base.Charsets;
import com.google.common.io.Closer;

@SuppressForbidden("legitimate sysstreams.")
public class DumpStreamsFromEventStream {
  public static void main(String[] args) throws Exception {
    File inputFile;

    if (args.length != 1) {
      System.err.println("Usage: [input.events]");
      System.exit(1);
      return;
    } else {
      inputFile = new File(args[0]);
    }

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
            IStreamEvent.class.cast(type.deserialize(input)).copyTo(syserr);
            break;
  
          case APPEND_STDOUT:
            IStreamEvent.class.cast(type.deserialize(input)).copyTo(sysout);
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
