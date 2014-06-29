package com.carrotsearch.ant.tasks.junit4.events;

import java.io.*;
import java.lang.annotation.Annotation;
import java.util.ArrayDeque;

import org.apache.commons.io.output.NullWriter;
import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.events.json.*;
import com.carrotsearch.ant.tasks.junit4.slave.SlaveMain;
import com.carrotsearch.randomizedtesting.Rethrow;
import com.google.common.base.Charsets;
import com.google.common.io.Closeables;
import com.google.common.io.CountingOutputStream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.stream.JsonWriter;

/**
 * Event serializer.
 */
public class Serializer implements Closeable {
  /**
   * Should help in ensuring the right order of stream writes.
   */
  private final Object lock = new Object();

  private Writer writer;
  private Gson gson;

  private final ArrayDeque<IEvent> events = new ArrayDeque<IEvent>();

  public final CountingOutputStream os;
  public Writer debug;

  public Serializer(OutputStream os) throws IOException {
    os = this.os = new CountingOutputStream(os);
    this.writer = new OutputStreamWriter(os, Charsets.UTF_8);
    this.gson = createGSon(Thread.currentThread().getContextClassLoader());
    this.debug = new NullWriter();
  }

  public Serializer serialize(IEvent event) throws IOException {
    SlaveMain.debug(debug, "Serializing: " + event.getType());
    try {
      synchronized (lock) {
        if (writer == null) {
          SlaveMain.debug(debug, "Serializer already closed.");
          throw new IOException("Serializer already closed.");
        }
  
        // An alternative way of solving GH-92 and GH-110. Instead of buffering
        // serialized json we emit directly. If a recursive call occurs to serialize()
        // we enqueue the event and continue, serializing them in order.
        
        events.addLast(event);
        if (events.size() > 1) {
          SlaveMain.debug(debug, "events.size() > 1, pass.");
          return this;
        }
  
        do {
          event = events.peekFirst();
          SlaveMain.debug(debug, "events.peek(): " + event.getType());
  
          try {
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setIndent("  ");
            jsonWriter.beginArray();
            jsonWriter.value(event.getType().name());
            gson.toJson(event, event.getClass(), jsonWriter);
            jsonWriter.endArray();
          } catch (Throwable t) {
            SlaveMain.debug(debug, "Error, nullifying writer: " + t.toString());
            for (StackTraceElement e : t.getStackTrace()) {
              SlaveMain.debug(debug, "  > " + e);
            }
            
            try {
              Closeables.close(writer, false);
            } catch (Throwable t2) {
              SlaveMain.debug(debug, "Error, closing writer: " + t2.toString());
              for (StackTraceElement e : t2.getStackTrace()) {
                SlaveMain.debug(debug, "  > " + e);
              }
            }
            writer = null;
  
            SlaveMain.warn("Unhandled exception in event serialization.", t);
            Rethrow.rethrow(t); // or skip?
          }
  
          events.removeFirst();
          writer.write("\n\n");
        } while (writer != null && !events.isEmpty());
  
        return this;
      }
    } finally {
      SlaveMain.debug(debug, "Serialized: " + event.getType());
    }
  }

  public Serializer flush() throws IOException {
    SlaveMain.debug(debug, "flush(): " + os.getCount());
    synchronized (lock) {
      if (writer != null) {
        writer.flush();
      }
      return this;
    }
  }

  public void close() throws IOException {
    synchronized (lock) {
      if (writer != null) {
        if (events.isEmpty()) {
          serialize(new QuitEvent());
        }
        writer.close();
        writer = null;
      }
    }
  }

  public static Gson createGSon(ClassLoader refLoader) {
    return new GsonBuilder()
      .registerTypeAdapter(byte[].class, new JsonByteArrayAdapter())
      .registerTypeHierarchyAdapter(Annotation.class, new JsonAnnotationAdapter(refLoader))
      .registerTypeHierarchyAdapter(Class.class, new JsonClassAdapter(refLoader))
      .registerTypeAdapter(Description.class, new JsonDescriptionAdapter())
      .setLongSerializationPolicy(LongSerializationPolicy.DEFAULT)
      .disableHtmlEscaping()
      .create();
  }  
}