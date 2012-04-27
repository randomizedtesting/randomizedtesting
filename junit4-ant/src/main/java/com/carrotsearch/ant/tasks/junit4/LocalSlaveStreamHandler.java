package com.carrotsearch.ant.tasks.junit4;

import java.io.*;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.taskdefs.StreamPumper;

import com.carrotsearch.ant.tasks.junit4.events.*;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

/**
 * Establish event passing with a subprocess and pump events to the bus.
 */
public class LocalSlaveStreamHandler implements ExecuteStreamHandler {
  private final EventBus eventBus;
  private final ClassLoader refLoader;
  private volatile BootstrapEvent bootstrapPacket;

  private InputStream stdout;
  private InputStream stderr;
  private OutputStreamWriter stdin;
  private final PrintStream warnStream;
  private final InputStream eventStream;
  
  private volatile boolean stopping;

  private ByteArrayOutputStream stderrBuffered = new ByteArrayOutputStream();
  private List<Thread> pumpers = Lists.newArrayList();

  public LocalSlaveStreamHandler(
      EventBus eventBus, ClassLoader classLoader, PrintStream warnStream, InputStream eventStream) {
    this.eventBus = eventBus;
    this.warnStream = warnStream;
    this.refLoader = classLoader;
    this.eventStream = eventStream;
  }

  @Override
  public void setProcessErrorStream(InputStream is) throws IOException {
    this.stderr = is;
  }
  
  @Override
  public void setProcessOutputStream(InputStream is) throws IOException {
    this.stdout = is;
  }

  @Override
  public void setProcessInputStream(OutputStream os) throws IOException {
    this.stdin = new OutputStreamWriter(
        os, Charset.defaultCharset());
  }
  
  @Override
  public void start() throws IOException {
    pumpers.add(new Thread(new StreamPumper(stderr, stderrBuffered), "pumper-stderr"));
    pumpers.add(new Thread(new StreamPumper(stdout, stderrBuffered), "pumper-stdout"));
    pumpers.add(new Thread(new Runnable() {
      public void run() {
        pumpEvents(eventStream);
      }
    }, "pumper-events"));

    // Start all pumper threads.
    UncaughtExceptionHandler handler = new UncaughtExceptionHandler() {
      public void uncaughtException(Thread t, Throwable e) {
        warnStream.println("Unhandled exception in thread: " + t);
        e.printStackTrace(warnStream);
      }
    };

    for (Thread t : pumpers) {
      t.setUncaughtExceptionHandler(handler);
      t.setDaemon(true);
      t.start();
    }
  }

  /**
   * Size of the error stream.
   */
  public boolean isErrorStreamNonEmpty() {
    return stderrBuffered.size() > 0;
  }

  /**
   * "error" stream from the forked process.
   */
  public String getErrorStreamAsString() {
    try {
      if (bootstrapPacket != null) {
        return new String(stderrBuffered.toByteArray(), bootstrapPacket.getDefaultCharsetName());
      }
    } catch (UnsupportedEncodingException e) {
      // Ignore.
    }
    return new String(stderrBuffered.toByteArray(), Charsets.US_ASCII);
  }

  /**
   * Pump events from event stream.
   */
  void pumpEvents(InputStream eventStream) {
    try {
      Deserializer deserializer = new Deserializer(eventStream, refLoader);

      IEvent event = null;
      while ((event = deserializer.deserialize()) != null) {
        try {
          switch (event.getType()) {
            case QUIT:
              eventBus.post(event);
              return;

            case IDLE:
              eventBus.post(new SlaveIdle(stdin));
              break;

            case BOOTSTRAP:
              bootstrapPacket = (BootstrapEvent) event;
              eventBus.post(event);
              break;

            default:
              eventBus.post(event);
          }
        } catch (Throwable t) {
          warnStream.println("Event bus dispatch error: " + t.toString());
          t.printStackTrace(warnStream);
        }
      }
    } catch (IOException e) {
      if (!stopping) {
        warnStream.println("Event stream error: " + e.toString());
        e.printStackTrace(warnStream);
      }
    }
  }
  
  @Override
  public void stop() {
    stopping = true;
    try {
      final int defaultDelay = 2000;
      for (Thread t : pumpers) {
        t.join(defaultDelay);
        t.interrupt();
      }
    } catch (InterruptedException e) {
      // Don't wait.
    }
  }
}
