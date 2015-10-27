package com.carrotsearch.ant.tasks.junit4.events;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;

import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonReader;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonWriter;
import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Nightly;

import static org.junit.Assert.*;

public class TestRemoteEventSerialization extends RandomizedTest {
  @RunWith(RandomizedRunner.class)
  @Nightly
  public static class Clazz1 {}
  public static class Clazz2 {}

  private Description description;
  private long start;
  private long end;
  private Failure failure;

  @Before
  public void setup() {
    Class<?> suiteClass = randomFrom(new Class<?> [] {
      Clazz1.class,
      Clazz2.class
    });
    description = Description.createSuiteDescription(suiteClass);
    
    Calendar c = Calendar.getInstance(Locale.ROOT);
    c.set(2015, 10, 20, 0, 0, 0);
    start = c.getTimeInMillis();
    end = start + randomIntBetween(0, 1000);
    
    String MARKER = "<exception-marker>";
    Throwable thrownException;
    try {
      throw new RuntimeException(MARKER);
    } catch (Throwable t) {
      thrownException = t;
    }

    failure = new Failure(description, thrownException);    
  }

  @Test
  public void eventBootstrap() throws IOException {
    checkRoundtrip(new BootstrapEvent());
  }

  @Test
  public void eventIdle() throws IOException {
    checkRoundtrip(new IdleEvent());
  }
  
  @Test
  public void eventQuit() throws IOException {
    checkRoundtrip(new QuitEvent());
  }

  @Test
  public void eventStdout() throws IOException {
    byte [] value = randomRealisticUnicodeOfCodepointLengthBetween(0, 10).getBytes(StandardCharsets.UTF_8);
    checkRoundtrip(new AppendStdOutEvent(value, 0, value.length));
  }
  
  @Test
  public void eventStderr() throws IOException {
    byte [] value = randomRealisticUnicodeOfCodepointLengthBetween(0, 10).getBytes(StandardCharsets.UTF_8);
    checkRoundtrip(new AppendStdErrEvent(value, 0, value.length));
  }

  
  @Test
  public void eventSuiteStarted() throws IOException {
    checkRoundtrip(new SuiteStartedEvent(description, System.currentTimeMillis()));
  }
  
  @Test
  public void eventSuiteFailure() throws IOException {
    checkRoundtrip(new SuiteFailureEvent(failure));
  }

  @Test
  public void eventSuiteCompleted() throws IOException {
    checkRoundtrip(new SuiteCompletedEvent(description, start, end));
  }

  @Test
  public void eventTestStartedEvent() throws IOException {
    checkRoundtrip(new TestStartedEvent(description));
  }

  @Test
  public void eventTestFailureEvent() throws IOException {
    checkRoundtrip(new TestFailureEvent(failure));
  }

  @Test
  public void eventTestIgnoredAssumptionEvent() throws IOException {
    checkRoundtrip(new TestIgnoredAssumptionEvent(failure));
  }

  @Test
  public void eventTestIgnoredEvent() throws IOException {
    checkRoundtrip(new TestIgnoredEvent(description, randomUnicodeOfLength(10)));
  }

  @Test
  public void eventTestFinishedEvent() throws IOException {
    checkRoundtrip(new TestFinishedEvent(description, start, end));
  }

  private void checkRoundtrip(RemoteEvent event) throws IOException {
    StringWriter sw = new StringWriter();

    final boolean lenient = randomBoolean();
    JsonWriter jw = new JsonWriter(sw);
    jw.setIndent("  ");
    jw.setLenient(lenient);
    event.serialize(jw);
    jw.close();

    String serialized1 = sw.toString();
    JsonReader jr = new JsonReader(new StringReader(serialized1));
    jr.setLenient(lenient);
    RemoteEvent deserialized = event.getType().deserialize(jr);

    
    // If we serialize again, the contents should be identical.
    sw.getBuffer().setLength(0);
    jw = new JsonWriter(sw);
    jw.setIndent("  ");
    jw.setLenient(lenient);
    deserialized.serialize(jw);
    jw.close();
    
    String serialized2 = sw.toString();
    if (!serialized2.equals(serialized1)) {
      fail("Roundtrip serialization failed:\n1: " + serialized1 + "\n2: " + serialized2);
    }
  }
}
