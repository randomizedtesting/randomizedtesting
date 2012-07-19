package com.carrotsearch.ant.tasks.junit4;

import java.io.*;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.taskdefs.StreamPumper;

import com.carrotsearch.ant.tasks.junit4.events.*;
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

  /** raw input stream to the client. */
  private OutputStream stdin;

  /** character-wrapped input stream to the client. */
  private OutputStreamWriter stdinWriter;

  private final PrintStream warnStream;
  private final InputStream eventStream;
  
  private volatile boolean stopping;

  private List<Thread> pumpers = Lists.newArrayList();

  private final OutputStream sysout;
  private final OutputStream syserr;
  private final long heartbeat;

  public LocalSlaveStreamHandler(
      EventBus eventBus, ClassLoader classLoader, PrintStream warnStream, InputStream eventStream,
      OutputStream sysout, OutputStream syserr, long heartbeat) {
    this.eventBus = eventBus;
    this.warnStream = warnStream;
    this.refLoader = classLoader;
    this.eventStream = eventStream;
    this.sysout = sysout;
    this.syserr = syserr;
    this.heartbeat = heartbeat;
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
    this.stdin = os;
  }

  /**
   * A timestamp of last received event (GH-106).
   */
  private volatile Long lastActivity;
  
  /**
   * Watchdog thread if heartbeat is to be measured.
   */
  private Thread watchdog;
  
  /**
   * Client charset extracted from {@link BootstrapEvent}.
   */
  private Charset clientCharset;

  @Override
  public void start() throws IOException {
    lastActivity = System.currentTimeMillis();

    pumpers.add(new Thread(new StreamPumper(stdout, sysout), "pumper-stdout"));
    pumpers.add(new Thread(new StreamPumper(stderr, syserr), "pumper-stderr"));
    pumpers.add(new Thread("pumper-events") {
      public void run() {
        pumpEvents(eventStream);
      }
    });

    if (heartbeat > 0) {
      pumpers.add(watchdog = new Thread("pumper-watchdog") {
        public void run() {
          final long heartbeatMillis = TimeUnit.SECONDS.toMillis(heartbeat);
          final long HEARTBEAT = Math.max(500, heartbeatMillis / 5);
          final long HEARTBEAT_EVENT_THRESHOLD = heartbeatMillis;
          try {
            long lastObservedUpdate = lastActivity;
            long reportDeadline = lastObservedUpdate + HEARTBEAT_EVENT_THRESHOLD; 
            while (true) {
              Thread.sleep(HEARTBEAT);
  
              Long last = lastActivity;
              if (last == null) {
                break; // terminated.
              }
  
              if (last != lastObservedUpdate) {
                lastObservedUpdate = last;
                reportDeadline = last + HEARTBEAT_EVENT_THRESHOLD;
              } else {
                final long current = System.currentTimeMillis();
                if (current >= reportDeadline) {
                  eventBus.post(new LowLevelHeartBeatEvent(last, current));
                  reportDeadline = System.currentTimeMillis() + HEARTBEAT_EVENT_THRESHOLD;
                }
              }
            }
          } catch (InterruptedException e ) {
            // terminate on interrupt.
          }
        }
      });
    }

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
        switch (event.getType()) {
          case APPEND_STDERR:
          case APPEND_STDOUT:
            // Ignore these two on activity heartbeats. GH-117
            break;
          default:
            lastActivity = System.currentTimeMillis();
            break;
        }

        try {
          switch (event.getType()) {
            case QUIT:
              eventBus.post(event);
              return;

            case IDLE:
              eventBus.post(new SlaveIdle(stdinWriter));
              break;

            case BOOTSTRAP:
              clientCharset = Charset.forName(((BootstrapEvent) event).getDefaultCharsetName());
              stdinWriter = new OutputStreamWriter(stdin, clientCharset);

              // fall through.
            default:
              eventBus.post(event);
          }
        } catch (Throwable t) {
          warnStream.println("Event bus dispatch error: " + t.toString());
          t.printStackTrace(warnStream);
        }
      }
      lastActivity = null;
    } catch (IOException e) {
      if (!stopping) {
        warnStream.println("Event stream error: " + e.toString());
        e.printStackTrace(warnStream);
      }
    }
  }
  
  @Override
  public void stop() {
    lastActivity = null;
    stopping = true;
    try {
      // Terminate watchdog early.
      if (watchdog != null) {
        watchdog.interrupt();
      }

      // Terminate all other pumpers.
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
