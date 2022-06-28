package com.carrotsearch.ant.tasks.junit4;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.carrotsearch.ant.tasks.junit4.events.BootstrapEvent;
import com.carrotsearch.ant.tasks.junit4.events.IEvent;
import com.carrotsearch.ant.tasks.junit4.events.IStreamEvent;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonWriter;
import com.carrotsearch.randomizedtesting.WriterOutputStream;

/**
 * Static forked JVM information.
 */
public final class ForkedJvmInfo {
  /**
   * Unique sequential identifier.
   */
  public final int id;

  /**
   * The number of executed forked JVMs, total.
   */
  public final int forkedJvmCount;

  /**
   * Bootstrap event.
   */
  private BootstrapEvent bootstrapEvent;

  /**
   * Timestamps for diagnostics.
   */
  long start, end;

  /**
   * Execute these test suites on this forked JVM.
   */
  ArrayList<String> testSuites;

  /**
   * Complete command line invocation string.
   */
  String forkedCommandLine;

  /**
   * Execution error if anything bad happened.
   */
  Throwable executionError;

  /* */
  public ForkedJvmInfo(int id, int forkedJvmCount) {
    this.id = id;
    this.forkedJvmCount = forkedJvmCount;
  }

  /**
   * Return the {@link Charset} used to encode stream bytes from the forked JVM.
   */
  public Charset getCharset() {
    if (bootstrapEvent != null) {
      return Charset.forName(bootstrapEvent.getDefaultCharsetName());
    } else {
      return Charset.defaultCharset();
    }
  }

  /**
   * System properties on the forked JVM.
   */
  public Map<String,String> getSystemProperties() {
    if (bootstrapEvent == null) {
      throw new RuntimeException("Bootstrap event not yet received.");
    }

    return bootstrapEvent.getSystemProperties();
  }

  /**
   * PID string of the forked JVM. May not be available or may come in an unknown format
   * (Java 8 will have real PID support, supposedly).
   */
  public String getPidString() {
    BootstrapEvent event = this.bootstrapEvent;
    if (event == null) {
      return "(?)";
    } else {
      return event.getPidString();
    }
  }

  /**
   * Command line string used to invoke the forked JVM
   */
  public String getCommandLine() {
    return forkedCommandLine;
  }
  
  /**
   * JVM name.
   */
  public String getJvmName() {
    return getSystemProperties().get("java.vm.name") + ", " +
           getSystemProperties().get("java.vm.version");
  }

  /**
   * Forked JVM execution time.
   */
  long getExecutionTime() {
    return end - start;
  }
  
  /**
   * Set the bootstrap event associated with this forked JVM.
   */
  void setBootstrapEvent(BootstrapEvent e) {
    this.bootstrapEvent = e;
  }

  /**
   * Filter through events looking for sysouts and syserrs and decode them
   * into a character streams. If both {@link Writer} arguments are the same object
   * the streams will be combined.  
   */
  public void decodeStreams(List<IEvent> events, Writer sysout, Writer syserr) throws IOException {
    int lineBuffer = 160; 
    WriterOutputStream stdout = new WriterOutputStream(sysout, getCharset(), lineBuffer, true);
    WriterOutputStream stderr = new WriterOutputStream(syserr, getCharset(), lineBuffer, true);
    for (IEvent evt : events) {
      switch (evt.getType()) {
        case APPEND_STDOUT:
          if (sysout != null) {
            ((IStreamEvent) evt).copyTo(stdout);
          }
          break;

        case APPEND_STDERR:
          if (syserr != null) {
            ((IStreamEvent) evt).copyTo(stderr);
          }
          break;

        default:
          break;
    }
    }

    stdout.flush();
    stderr.flush();
  }

  public void serialize(JsonWriter w) throws IOException {
    w.beginObject();

    w.name("id").value(id);
    w.name("jvmName").value(getJvmName());
    w.name("charset").value(getCharset().displayName(Locale.ROOT));
    w.name("commandLine").value(getCommandLine());

    w.name("systemProperties").beginObject();
    for (Map.Entry<String, String> e : getSystemProperties().entrySet()) {
      w.name(e.getKey()).value(e.getValue());
    }
    w.endObject();

    w.endObject();
  }
}
