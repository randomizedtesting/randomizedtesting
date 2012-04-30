package com.carrotsearch.ant.tasks.junit4.events;

import java.io.*;
import java.lang.annotation.Annotation;

import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.events.json.*;
import com.carrotsearch.ant.tasks.junit4.slave.SlaveMain;
import com.carrotsearch.randomizedtesting.Rethrow;
import com.google.common.base.Charsets;
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

  private Writer writer2;
  private Gson gson;

  public Serializer(OutputStream os) throws IOException {
    this.writer2 = new OutputStreamWriter(os, Charsets.UTF_8);
    this.gson = createGSon(Thread.currentThread().getContextClassLoader());
  }

  public Serializer serialize(IEvent event) throws IOException {
    synchronized (lock) {
      if (writer2 == null) {
        throw new IOException("Serializer already closed.");
      }
      
      // Attempt to serialize event atomically to a top-level value.
      // See GH-92 for more info.
      final StringWriter sw = new StringWriter();
      try {
        final JsonWriter jsonWriter = new JsonWriter(sw);
        jsonWriter.setIndent("  ");
        jsonWriter.beginArray();
        jsonWriter.value(event.getType().name());
        gson.toJson(event, event.getClass(), jsonWriter);
        jsonWriter.endArray();
        jsonWriter.close();
      } catch (Throwable t) {
        SlaveMain.warn("Unhandled exception in event serialization.", t);
        Rethrow.rethrow(t); // or skip?
      }

      writer2.write(sw.toString());
      writer2.write("\n\n");
      return this;
    }
  }

  public Serializer flush() throws IOException {
    synchronized (lock) {
      if (writer2 != null) {
        writer2.flush();
      }
      return this;
    }
  }

  public void close() throws IOException {
    synchronized (lock) {
      if (writer2 != null) {
        serialize(new QuitEvent());
        writer2.close();
        writer2 = null;
      }
    }
  }

  static Gson createGSon(ClassLoader refLoader) {
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