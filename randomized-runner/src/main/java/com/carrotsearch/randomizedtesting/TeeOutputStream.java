package com.carrotsearch.randomizedtesting;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Custom teeing stream that ensures both streams receive the output and 
 * no exceptions are quietly suppressed. 
 */
public class TeeOutputStream extends OutputStream {
  private final OutputStream first;
  private final OutputStream second;

  public TeeOutputStream(OutputStream first, OutputStream second) {
    this.first = first;
    this.second = second;
  }
  
  @Override
  public void write(int b) throws IOException {
    IOException ex = null;

    try {
      first.write(b);
    } catch (IOException e) {
      ex = e;
    }

    try {
      second.write(b);
    } catch (IOException e) {
      if (ex != null) {
        ex.addSuppressed(e);
      } else {
        ex = e;
      }
    }
    
    if (ex != null) {
      throw ex;
    }
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    IOException ex = null;

    try {
      first.write(b, off, len);
    } catch (IOException e) {
      ex = e;
    }

    try {
      second.write(b, off, len);
    } catch (IOException e) {
      if (ex != null) {
        ex.addSuppressed(e);
      } else {
        ex = e;
      }
    }
    
    if (ex != null) {
      throw ex;
    }
  }
  
  @Override
  public void close() throws IOException {
    IOException ex = null;

    try {
      first.close();
    } catch (IOException e) {
      ex = e;
    }

    try {
      second.close();
    } catch (IOException e) {
      if (ex != null) {
        ex.addSuppressed(e);
      } else {
        ex = e;
      }
    }
    
    if (ex != null) {
      throw ex;
    }
  }
  
  @Override
  public void flush() throws IOException {
    IOException ex = null;

    try {
      first.flush();
    } catch (IOException e) {
      ex = e;
    }

    try {
      second.flush();
    } catch (IOException e) {
      if (ex != null) {
        ex.addSuppressed(e);
      } else {
        ex = e;
      }
    }
    
    if (ex != null) {
      throw ex;
    }
  }
  
  @Override
  public void write(byte[] b) throws IOException {
    IOException ex = null;

    try {
      first.write(b);
    } catch (IOException e) {
      ex = e;
    }

    try {
      second.write(b);
    } catch (IOException e) {
      if (ex != null) {
        ex.addSuppressed(e);
      } else {
        ex = e;
      }
    }
    
    if (ex != null) {
      throw ex;
    }
  }

  @Override
  public String toString() {
    return "[tee: 1:" + first + ", 2:" + second + "]";
  }

  @Override
  public boolean equals(Object obj) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public int hashCode() {
    throw new UnsupportedOperationException();
  }
  
  @Override
  protected Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
