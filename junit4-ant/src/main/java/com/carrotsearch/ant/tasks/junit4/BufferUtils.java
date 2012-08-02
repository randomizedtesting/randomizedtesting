package com.carrotsearch.ant.tasks.junit4;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public final class BufferUtils {
  private BufferUtils() {}

  public static void copyTo(ByteBuffer bb, OutputStream out) throws IOException {
    if (bb.hasArray()) {
      out.write(bb.array(), bb.arrayOffset() + bb.position(), bb.remaining());
      bb.position(bb.limit());
    } else {
      for (int i = bb.remaining(); i > 0; i--) {
        out.write(bb.get());
      }
    }
  }
}
