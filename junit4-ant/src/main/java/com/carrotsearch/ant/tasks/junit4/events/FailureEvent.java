package com.carrotsearch.ant.tasks.junit4.events;

import java.io.IOException;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonReader;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonWriter;

/**
 * Generic serialized failure event.
 */
public abstract class FailureEvent extends AbstractEvent implements IDescribable {
  private FailureMirror failure;

  public FailureEvent(EventType type) {
    super(type);
  }

  protected void setFailure(Failure failure) {
    if (this.failure != null) {
      throw new IllegalStateException("Set only once.");
    }

    this.failure = new FailureMirror(failure);
  }

  public FailureMirror getFailure() {
    return failure;
  }
  
  public Description getDescription() {
    return failure.getDescription();
  }
  
  @Override
  public void serialize(JsonWriter writer) throws IOException {
    writer.beginObject();

    writer.name("description");
    writeDescription(writer, failure.getDescription());

    writer.name("message").value(failure.getMessage());
    writer.name("trace").value(failure.getTrace());
    writer.name("throwableString").value(failure.getThrowableString());
    writer.name("throwableClass").value(failure.getThrowableClass());
    writer.name("assertionViolation").value(failure.isAssertionViolation());
    writer.name("assumptionViolation").value(failure.isAssumptionViolation());

    writer.endObject();
  }

  @Override
  public void deserialize(JsonReader reader) throws IOException {
    reader.beginObject();

    expectProperty(reader, "description");
    Description description = JsonHelpers.readDescription(reader);
    String message = readStringOrNullProperty(reader, "message");
    String trace = readStringOrNullProperty(reader, "trace");
    String throwableString = readStringOrNullProperty(reader, "throwableString");
    String throwableClass = readStringOrNullProperty(reader, "throwableClass");
    boolean assertionViolation = readBoolean(reader, "assertionViolation");
    boolean assumptionViolation = readBoolean(reader, "assumptionViolation");

    this.failure = new FailureMirror(description, 
                                     message, 
                                     trace, 
                                     throwableString, 
                                     throwableClass, 
                                     assertionViolation, 
                                     assumptionViolation);
    reader.endObject();
  }  
}
