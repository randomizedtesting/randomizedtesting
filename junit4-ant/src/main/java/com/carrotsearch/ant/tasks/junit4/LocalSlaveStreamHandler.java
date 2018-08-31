package com.carrotsearch.ant.tasks.junit4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.taskdefs.StreamPumper;

import com.carrotsearch.ant.tasks.junit4.events.BootstrapEvent;
import com.carrotsearch.ant.tasks.junit4.events.Deserializer;
import com.carrotsearch.ant.tasks.junit4.events.EventType;
import com.carrotsearch.ant.tasks.junit4.events.IEvent;
import com.carrotsearch.ant.tasks.junit4.events.IStreamEvent;
import com.carrotsearch.ant.tasks.junit4.events.LowLevelHeartBeatEvent;
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
  private final TailInputStream eventStream;
  
  private volatile boolean stopping;

  private List<Thread> pumpers = new ArrayList<>();

  private final OutputStream sysout;
  private final OutputStream syserr;
  private final long heartbeat;
  private final RandomAccessFile streamsBuffer;
  private final OutputStream streamsBufferWrapper;

  public LocalSlaveStreamHandler(
      EventBus eventBus, ClassLoader classLoader, PrintStream warnStream, TailInputStream eventStream,
      OutputStream sysout, OutputStream syserr, long heartbeat, final RandomAccessFile streamsBuffer) {
    this.eventBus = eventBus;
    this.warnStream = warnStream;
    this.refLoader = classLoader;
    this.eventStream = eventStream;
    this.sysout = sysout;
    this.syserr = syserr;
    this.heartbeat = heartbeat;
    this.streamsBuffer = streamsBuffer;
    this.streamsBufferWrapper = new OutputStream() {
      @Override
      public void write(int b) throws IOException {
        streamsBuffer.write(b);
      }

      @Override
      public void write(byte[] b) throws IOException {
        streamsBuffer.write(b, 0, b.length);
      }

      @Override
      public void write(byte[] b, int off, int len) throws IOException {
        streamsBuffer.write(b, off, len);
      }
    };
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

    pumpers.add(new Thread(new SimpleStreamPumper(stdout, sysout), "pumper-stdout"));
    pumpers.add(new Thread(new SimpleStreamPumper(stderr, syserr), "pumper-stderr"));
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

  private static class OnDiskStreamEvent implements IEvent, IStreamEvent {
    private final RandomAccessFile bufferFile;
    private long start;
    private long end;
    private EventType type;

    public OnDiskStreamEvent(EventType type, RandomAccessFile streamsBuffer, long start, long end) {
      this.bufferFile = streamsBuffer;
      this.start = start;
      this.end = end;
      this.type = type;
    }

    @Override
    public EventType getType() {
      return type;
    }

    @Override
    public void copyTo(OutputStream os) throws IOException {
      final long restorePosition = bufferFile.getFilePointer();
      bufferFile.seek(start);
      try {
        long length = end - start;
        final byte [] buffer = new byte [(int) Math.min(length, 1024 * 4)];
        while (length > 0) {
          int bytes = bufferFile.read(buffer, 0, (int) Math.min(length, buffer.length));
          os.write(buffer, 0, bytes);
          length -= bytes;
        }
      } finally {
        bufferFile.seek(restorePosition);
      }
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
              eventBus.post(event);
              break;

            case APPEND_STDERR:
            case APPEND_STDOUT:
              assert streamsBuffer.getFilePointer() == streamsBuffer.length();
              final long bufferStart = streamsBuffer.getFilePointer();
              IStreamEvent streamEvent = (IStreamEvent) event;
              streamEvent.copyTo(streamsBufferWrapper);
              final long bufferEnd = streamsBuffer.getFilePointer();

              event = new OnDiskStreamEvent(event.getType(), streamsBuffer, bufferStart, bufferEnd);
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
      lastActivity = null;
    } catch (Throwable e) {
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

      // Wait for all stream pumpers.
      eventStream.completeAtEnd();
      for (Thread t : pumpers) {
        t.join();
        t.interrupt();
      }
    } catch (InterruptedException e) {
      // Don't wait.
    }
  }
}
