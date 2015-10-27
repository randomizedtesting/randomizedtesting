package com.carrotsearch.ant.tasks.junit4.events;

import java.io.IOException;

import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonReader;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonWriter;

public class TestIgnoredEvent extends AbstractEventWithDescription {
  private long startTimestamp;
  private String cause;

  protected TestIgnoredEvent() {
    super(EventType.TEST_IGNORED);
  }
  
  public TestIgnoredEvent(Description description, String cause) {
    this();
    setDescription(description);

    // For ignored tests, take the current time as the execution timestamp.
    this.startTimestamp = System.currentTimeMillis();
    this.cause = cause;
  }

  public long getStartTimestamp() {
    return startTimestamp;
  }

  public String getCause() {
    return cause;
  }
  
  @Override
  public void serialize(JsonWriter writer) throws IOException {
    writer.beginObject();
    
    writer.name("description");
    super.serialize(writer);

    writer.name("startTimestamp").value(startTimestamp);
    writer.name("cause").value(cause);
    
    writer.endObject();
  }

  @Override
  public void deserialize(JsonReader reader) throws IOException {
    reader.beginObject();
    
    expectProperty(reader, "description");
    super.deserialize(reader);
    
    startTimestamp = readLongProperty(reader, "startTimestamp");
    cause = readStringOrNullProperty(reader, "cause");

    reader.endObject();
  }
}
