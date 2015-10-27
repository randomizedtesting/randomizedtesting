package com.carrotsearch.ant.tasks.junit4.events;

import java.io.IOException;

import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonReader;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonWriter;


public class TestFinishedEvent extends AbstractEventWithDescription {
  private long executionTime;
  private long startTimestamp;

  protected TestFinishedEvent() {
    super(EventType.TEST_FINISHED);
  }

  public TestFinishedEvent(Description description, long timeMillis, long startTimestamp) {
    this();
    this.executionTime = timeMillis;
    this.startTimestamp = startTimestamp;
    setDescription(description);
  }

  public long getExecutionTime() {
    return executionTime;
  }

  public long getStartTimestamp() {
    return startTimestamp;
  }
  
  @Override
  public void serialize(JsonWriter writer) throws IOException {
    writer.beginObject();
    
    writer.name("description");
    super.serialize(writer);

    writer.name("startTimestamp").value(startTimestamp);
    writer.name("executionTime").value(executionTime);
    
    writer.endObject();
  }

  @Override
  public void deserialize(JsonReader reader) throws IOException {
    reader.beginObject();
    
    expectProperty(reader, "description");
    super.deserialize(reader);
    
    startTimestamp = readLongProperty(reader, "startTimestamp");
    executionTime =  readLongProperty(reader, "executionTime");

    reader.endObject();
  }  
}
