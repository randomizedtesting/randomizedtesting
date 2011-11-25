package com.carrotsearch.ant.tasks.junit4;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;

import org.apache.commons.io.output.WriterOutputStream;

import com.carrotsearch.ant.tasks.junit4.events.AppendStdErrEvent;
import com.carrotsearch.ant.tasks.junit4.events.AppendStdOutEvent;
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
   * {@link Charset} as used on the client. We assume slave and master are
   * equally equipped with {@link Charset} and encoders/ decoders.
   */
  private Charset charset;

  /* */
  public SlaveInfo(int id, int slaves) {
    this.id = id;
    this.slaves = slaves;
  }

  /**
   * Return the {@link Charset} used to encode stream bytes from the slave.
   */
  public Charset getCharset() {
    return charset;
  }

  /**
   * Set the slave's charset.
   */
  void setCharset(String charsetName) throws UnsupportedCharsetException {
    this.charset = Charset.forName(charsetName);
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
