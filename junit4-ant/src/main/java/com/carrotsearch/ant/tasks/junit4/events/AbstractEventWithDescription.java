package com.carrotsearch.ant.tasks.junit4.events;

import java.io.IOException;

import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonReader;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonWriter;

abstract class AbstractEventWithDescription extends AbstractEvent implements IDescribable {
  private Description description;

  public AbstractEventWithDescription(EventType type) {
    super(type);
  }

  public Description getDescription() {
    return description;
  }
  
  protected void setDescription(Description description) {
    if (this.description != null)
      throw new IllegalStateException("Initialize once.");
    this.description = description;
  }  
  
  @Override
  public void serialize(JsonWriter writer) throws IOException {
    writeDescription(writer, description);
  }

  @Override
  public void deserialize(JsonReader reader) throws IOException {
    this.description = JsonHelpers.readDescription(reader);
  }
}
