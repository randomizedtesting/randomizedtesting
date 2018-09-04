package com.carrotsearch.ant.tasks.junit4.events;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonWriter;
import com.carrotsearch.ant.tasks.junit4.slave.SlaveMain;
import com.google.common.base.Charsets;

/**
 * Event serializer.
 */
public class Serializer implements Closeable {
  /**
   * Should help in ensuring the right order of stream writes.
   */
  private final Object lock = new Object();

  private Writer writer;
  private JsonWriter jsonWriter;

  private final ArrayDeque<RemoteEvent> events = new ArrayDeque<RemoteEvent>();

  private volatile Throwable doForcedShutdown;
  private Thread forceCloseDaemon;
  private AtomicBoolean forceCloseDaemonQuit = new AtomicBoolean();

  public Serializer(OutputStream os) throws IOException {
    this.writer = new OutputStreamWriter(os, Charsets.UTF_8);
    this.jsonWriter = new JsonWriter(writer);
    this.jsonWriter.setIndent(" ");
    this.jsonWriter.setLenient(true);
    this.forceCloseDaemon = new Thread("JUnit4-serializer-daemon") {
      {
        this.setDaemon(true);
      }
      
      @Override
      public void run() {
        try {
          while (!forceCloseDaemonQuit.get()) {
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              // Ignore.
            }

            Throwable reason = doForcedShutdown;
            if (reason != null) {
              try {
                SlaveMain.warn("Unhandled exception in event serialization.", reason);
              } finally {
                Runtime.getRuntime().halt(0);
              }
            }
          }
        } catch (Throwable t) {
          SlaveMain.warn("Unreachable code. Complete panic.", t);
        }
      }
      
      @Override
      public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return new UncaughtExceptionHandler() {
          @Override
          public void uncaughtException(Thread t, Throwable e) {
            SlaveMain.warn("Unreachable code. Complete panic.", e);
          }
        };
      }
    };

    forceCloseDaemon.start();
  }

  public Serializer serialize(RemoteEvent event) throws IOException {
    synchronized (lock) {
      if (writer == null) {
        throw new IOException("Serializer already closed.");
      }

      // An alternative way of solving GH-92 and GH-110. Instead of buffering
      // serialized json we emit directly. If a recursive call occurs to serialize()
      // we enqueue the event and continue, serializing them in order.
      events.addLast(event);
      if (events.size() > 1) {
        // SlaveMain.warn("Serializing " + event.getType() + " (postponed, " + events.size() + " in queue)", null);
        return this;
      }

      flushQueue();

      return this;
    }
  }

  private final AtomicBoolean isFlushing = new AtomicBoolean();
  private void flushQueue() throws IOException {
    if (!Thread.holdsLock(lock)) throw new IllegalStateException("Must be holding a lock on flushing.");

    if (isFlushing.getAndSet(true)) {
      // We're already flushing, return.
      // SlaveMain.warn("Flush queue already flushing", null);
      return;
    }

    // SlaveMain.warn("Flush queue start", null);
    try {
      while (!events.isEmpty()) {
        if (writer == null) {
          throw new IOException("Serializer already closed, with " + events.size() + " events on queue.");
        }

        final RemoteEvent event = events.removeFirst();
        try {
          // SlaveMain.warn("Serializing " + event.getType(), null);
          AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws Exception {
              jsonWriter.beginArray();
              jsonWriter.value(event.getType().name());
              event.serialize(jsonWriter);
              jsonWriter.endArray();
              writer.write("\n\n");
              return null;
            }
          });
        } catch (Throwable t) {
          doForcedShutdown = t;
          break;
        }
      }
    } finally {
      isFlushing.set(false);
    }
    // SlaveMain.warn("Flush queue end", null);

    if (doForcedShutdown != null) {
      // We can't do a stack bang here so any call is a risk of hitting SOE again.
      while (true) {
        try {
          forceCloseDaemon.join();
        } catch (Throwable ignored) {
          // Ignore.
        }
      }
    }
  }

  public Serializer flush() throws IOException {
    synchronized (lock) {
      if (writer != null) {
        // SlaveMain.warn("flushing...", null);
        flushQueue();
        writer.flush();
      } else {
        // SlaveMain.warn("flushing failed (serializer closed)", null);
      }
      return this;
    }
  }

  public void close() throws IOException {
    synchronized (lock) {
      // SlaveMain.warn("closing...", null);
      if (writer != null) {
        serialize(new QuitEvent());
        flushQueue();

        writer.close();
        writer = null;
      }
      forceCloseDaemonQuit.set(true);
    }

    try {
      forceCloseDaemon.interrupt();
      forceCloseDaemon.join();
    } catch (InterruptedException e) {
      // Ignore, can't do much about it (shouldn't happen).
    }    
  }
}