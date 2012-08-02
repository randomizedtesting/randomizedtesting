package com.carrotsearch.ant.tasks.junit4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

public class TestBufferUtils extends RandomizedTest {
  @Test
  @Repeat(iterations = 100)
  public void testCopyRandomized() throws Exception {
    byte [] bytes = randomAsciiOfLength(1000).getBytes("UTF-8");

    int offset = randomIntBetween(0, bytes.length);
    int length = randomIntBetween(0, bytes.length - offset);

    check(bytes, offset, length);
  }

  @Test
  public void testCopyBoundaryConditions() throws Exception {
    byte [] bytes = randomAsciiOfLength(1000).getBytes("UTF-8");

    check(bytes, 0, bytes.length);
    check(bytes, 0, 1);
    check(bytes, 0, 0);
    check(bytes, bytes.length, 0);
  }

  private void check(byte[] bytes, int offset, int length) throws IOException {
    ByteBuffer bb = ByteBuffer.wrap(bytes, offset, length);
    if (randomBoolean()) {
      bb = bb.asReadOnlyBuffer();
      Assert.assertFalse(bb.hasArray());
    } else {
      Assert.assertTrue(bb.hasArray());
    }

    // Occasionally read a few bytes from the buffer to move position.
    if (rarely()) {
      for (int i = randomIntBetween(0, length); i > 0; i--, length--, offset++) {
        bb.get();
      }
    }

    byte [] expectedSlice = new byte [length];
    System.arraycopy(bytes, offset, expectedSlice, 0, length);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BufferUtils.copyTo(bb, baos);

    // Assert identical data.
    Assert.assertArrayEquals(expectedSlice, baos.toByteArray());
    Assert.assertFalse(bb.hasRemaining());
  }
}
