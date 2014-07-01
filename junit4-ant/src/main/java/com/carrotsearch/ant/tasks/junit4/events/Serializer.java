package com.carrotsearch.ant.tasks.junit4.events;

import java.io.*;
import java.lang.annotation.Annotation;
import java.util.ArrayDeque;

import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.events.json.*;
import com.carrotsearch.ant.tasks.junit4.slave.SlaveMain;
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

  private Writer writer;
  private Gson gson;

  private final ArrayDeque<IEvent> events = new ArrayDeque<IEvent>();
  
  private volatile Throwable doForcedShutdown;
  private Thread forceCloseDaemon;

  public Serializer(OutputStream os) throws IOException {
    this.writer = new OutputStreamWriter(os, Charsets.UTF_8);
    this.gson = createGSon(Thread.currentThread().getContextClassLoader());

    this.forceCloseDaemon = new Thread("JUnit4-serializer-daemon") {
      {
        this.setDaemon(true);
      }
      
      @Override
      public void run() {
        try {
          while (true) {
            Thread.sleep(1000);
            Throwable reason = doForcedShutdown;
            if (reason != null) {
              try {
                SlaveMain.warn("Unhandled exception in event serialization.", reason);
              } finally {
                Runtime.getRuntime().halt(0);
              }
            }
          }
        } catch (InterruptedException e) {
          // Ignore and exit.
        }
      }
    };
    forceCloseDaemon.start();
  }

  public Serializer serialize(IEvent event) throws IOException {
    synchronized (lock) {
      if (writer == null) {
        throw new IOException("Serializer already closed.");
      }

      do {
        // An alternative way of solving GH-92 and GH-110. Instead of buffering
        // serialized json we emit directly. If a recursive call occurs to serialize()
        // we enqueue the event and continue, serializing them in order.
        events.addLast(event);
        if (events.size() > 1) {
          return this;
        }

        event = events.peekFirst();

        try {
          JsonWriter jsonWriter = new JsonWriter(writer);
          jsonWriter.setIndent("  ");
          jsonWriter.beginArray();
          jsonWriter.value(event.getType().name());
          gson.toJson(event, event.getClass(), jsonWriter);
          jsonWriter.endArray();
          writer.write("\n\n");
        } catch (Throwable t) {
          // We can't do a stack bang here so any call is a risk of hitting SOE again.
          while (true) {
            doForcedShutdown = t;
            try {
              forceCloseDaemon.join();
            } catch (Throwable ignored) {
              // Ignore.
            }
          }
        }

        events.removeFirst();
      } while (writer != null && !events.isEmpty());

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
        if (events.isEmpty()) {
          serialize(new QuitEvent());
        }
        writer.close();
        writer = null;
      }

      try {
        forceCloseDaemon.interrupt();
        forceCloseDaemon.join();
      } catch (InterruptedException e) {
        // Ignore, can't do much about it (shouldn't happen).
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