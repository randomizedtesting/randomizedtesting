package com.carrotsearch.ant.tasks.junit4;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.WriterOutputStream;

import com.carrotsearch.ant.tasks.junit4.events.AppendStdErrEvent;
import com.carrotsearch.ant.tasks.junit4.events.AppendStdOutEvent;
import com.carrotsearch.ant.tasks.junit4.events.BootstrapEvent;
import com.carrotsearch.ant.tasks.junit4.events.IEvent;

/**
 * Static slave information.
 */
public final class SlaveInfo {
  /**
   * Unique sequential slave identifier.
   */
  public final int id;

  /**
   * The number of executed slaves, total.
   */
  public final int slaves;

  /**
   * Bootstrap event.
   */
  private BootstrapEvent bootstrapEvent;

  /**
   * Timestamps for diagnostics.
   */
  long start, end;

  /* */
  public SlaveInfo(int id, int slaves) {
    this.id = id;
    this.slaves = slaves;
  }

  /**
   * Return the {@link Charset} used to encode stream bytes from the slave.
   */
  public Charset getCharset() {
    return Charset.forName(bootstrapEvent.getDefaultCharsetName());
  }

  /**
   * System properties on the slave.
   */
  public Map<String,String> getSystemProperties() {
    return bootstrapEvent.getSystemProperties();
  }

  /**
   * JVM name (slave).
   */
  public String getJvmName() {
    return getSystemProperties().get("java.vm.name") + ", " +
           getSystemProperties().get("java.vm.version");
  }

  /**
   * Slave execution time.
   */
  long getExecutionTime() {
    return end - start;
  }
  
  /**
   * Set the bootstrap event associated with this slave. 
   */
  void setBootstrapEvent(BootstrapEvent e) {
    this.bootstrapEvent = e;
  }

  /**
   * Filter through events looking for sysouts and syserrs and decode them
   * into a character streams. If both {@link Writer} arguments are the same object
   * the streams will be combined.  
   */
  public void decodeStreams(List<IEvent> events, Writer sysout, Writer syserr) {
    int lineBuffer = 160; 
    WriterOutputStream stdout = new WriterOutputStream(sysout, getCharset(), lineBuffer, true);
    WriterOutputStream stderr = new WriterOutputStream(syserr, getCharset(), lineBuffer, true);
    for (IEvent evt : events) {
      try {
        switch (evt.getType()) {
          case APPEND_STDOUT:
            if (sysout != null) {
              stdout.write(((AppendStdOutEvent) evt).getChunk());
            }
            break;

          case APPEND_STDERR:
            if (syserr != null) {
              stderr.write(((AppendStdErrEvent) evt).getChunk());
            }
            break;
        }
      } catch (IOException e) {
        // Ignore.
      }
    }

    try {
      stdout.flush();
      stderr.flush();
    } catch (IOException e) {
      // Ignore.
    }
  }
}
