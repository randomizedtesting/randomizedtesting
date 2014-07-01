package com.carrotsearch.ant.tasks.junit4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.ant.tasks.junit4.events.AppendStdErrEvent;
import com.carrotsearch.ant.tasks.junit4.events.Deserializer;
import com.carrotsearch.ant.tasks.junit4.events.IEvent;
import com.carrotsearch.ant.tasks.junit4.events.Serializer;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

public class TestJsonByteArrayRoundtrip extends RandomizedTest {
  @Test
  @Repeat(iterations = 100)
  public void testRoundTrip() throws Exception {
    byte[] bytes = new byte[randomIntBetween(0, 1024)];
    getRandom().nextBytes(bytes);

    check(bytes);
  }

  @Test
  public void testSimpleAscii() throws Exception {
    check("ABCabc0123".getBytes("UTF-8"));
    check("\n\t".getBytes("UTF-8"));
  }

  private void check(byte[] bytes) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Serializer s = new Serializer(baos);
    s.serialize(new AppendStdErrEvent(bytes, 0, bytes.length));
    s.flush();
    s.close();

    Deserializer deserializer = new Deserializer(new ByteArrayInputStream(baos.toByteArray()),
        Thread.currentThread().getContextClassLoader());
    IEvent deserialize = deserializer.deserialize();
    
    Assert.assertTrue(deserialize instanceof AppendStdErrEvent);
    AppendStdErrEvent e = ((AppendStdErrEvent) deserialize);
    baos.reset();
    e.copyTo(baos);
    Assert.assertTrue(
        "Exp: " + Arrays.toString(bytes) + "\n" +
        "was: " + Arrays.toString(baos.toByteArray()),
        Arrays.equals(bytes, baos.toByteArray()));
  }
}
