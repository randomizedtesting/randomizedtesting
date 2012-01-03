package com.carrotsearch.ant.tasks.junit4.spikes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;
import org.junit.runner.*;
import org.junit.runner.notification.Failure;

import com.carrotsearch.ant.tasks.junit4.events.*;
import com.carrotsearch.randomizedtesting.annotations.*;
import com.google.common.base.Charsets;

public class LocalRun {
  public static class TestClass {
    @Test
    @Seeds({@Seed("deadbeef"), @Seed()})
    @Repeat(iterations = 10, useConstantSeed = true)
    public void testMe() {
      throw new RuntimeException("Failure!");
    }
  }

  public static void main(String[] args) throws Exception {
    Description complex = Request.aClass(TestClass.class).getRunner().getDescription();
    Failure failure = JUnitCore.runClasses(TestClass.class).getFailures().get(0);

    IEvent [] events = {
        //new BootstrapEvent(BootstrapEvent.EventChannelType.STDOUT),
        //new QuitEvent(),
        new SuiteStartedEvent(complex),
        new TestStartedEvent(complex),
        new TestFinishedEvent(complex, 0, 100),
        new SuiteFailureEvent(failure),
        new SuiteCompletedEvent(complex, 100, -1L)
        //new SuiteFailureEvent(failure)
        //new SuiteCompletedEvent(complex, 100, -1L)
        //new AppendStdOutEvent(new byte [] {-1, 0, 1, 2, 3}, 0, 4),
        //new AppendStdErrEvent(new byte [] {-1, 0, 1, 2, 3}, 0, 4),
        //new TestFailureEvent(failure)
        //new TestIgnoredAssumptionEvent(failure)
        //new TestIgnoredEvent(complex)
    };

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    Serializer serializer = new Serializer(os);
    for (IEvent event : events) {
      serializer.serialize(event);
    }
    serializer.close();

    System.out.println(new String(os.toByteArray(), Charsets.UTF_8));
    System.out.println("\n---");

    Deserializer deserializer = new Deserializer(new ByteArrayInputStream(os.toByteArray()), 
        Thread.currentThread().getContextClassLoader());
    os.reset();
    IEvent event = null;
    while ((event = deserializer.deserialize()) != null) {
      System.out.println("Event: " + event);
      serializer = new Serializer(os);
      serializer.serialize(event);
      serializer.flush();
      System.out.println(new String(os.toByteArray(), Charsets.UTF_8));
      os.reset();
    }
  }
}
