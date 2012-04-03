package com.carrotsearch.ant.tasks.junit4.events;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;

import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.events.json.*;
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

  private JsonWriter writer;
  private Gson gson;

  public Serializer(OutputStream os) throws IOException {
    this.writer = new JsonWriter(new OutputStreamWriter(os, Charsets.UTF_8));
    this.writer.setIndent("  ");
    this.writer.beginArray();

    this.gson = createGSon(Thread.currentThread().getContextClassLoader());
  }

  public Serializer serialize(IEvent event) throws IOException {
    synchronized (lock) {
      if (writer == null) {
        throw new IOException("Serializer already closed.");
      }
      writer.beginArray();
      writer.value(event.getType().name());
      gson.toJson(event, event.getClass(), writer);
      writer.endArray();
      return this;
    }
  }

  public Serializer flush() throws IOException {
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
        serialize(new QuitEvent());
        writer.endArray();
        writer.close();
        writer = null;
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