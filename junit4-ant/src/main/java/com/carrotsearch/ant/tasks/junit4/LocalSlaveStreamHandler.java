package com.carrotsearch.ant.tasks.junit4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.taskdefs.StreamPumper;

import com.carrotsearch.ant.tasks.junit4.events.Deserializer;
import com.carrotsearch.ant.tasks.junit4.events.IEvent;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

/**
 * Establish event passing with a subprocess and pump events to the bus.
 */
public class LocalSlaveStreamHandler implements ExecuteStreamHandler {
  private final EventBus eventBus;
  private final ClassLoader refLoader;

  private InputStream stdout;
  private InputStream stderr;
  private OutputStreamWriter stdin;
  private final PrintStream warnStream;
  private final InputStream eventStream;
  
  private volatile boolean stopping;

  private List<Thread> pumpers = Lists.newArrayList();

  private final OutputStream sysout;
  private final OutputStream syserr;

  public LocalSlaveStreamHandler(
      EventBus eventBus, ClassLoader classLoader, PrintStream warnStream, InputStream eventStream,
      OutputStream sysout, OutputStream syserr) {
    this.eventBus = eventBus;
    this.warnStream = warnStream;
    this.refLoader = classLoader;
    this.eventStream = eventStream;
    this.sysout = sysout;
    this.syserr = syserr;
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
    pumpers.add(new Thread(new StreamPumper(stdout, sysout), "pumper-stdout"));
    pumpers.add(new Thread(new StreamPumper(stderr, syserr), "pumper-stderr"));
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
