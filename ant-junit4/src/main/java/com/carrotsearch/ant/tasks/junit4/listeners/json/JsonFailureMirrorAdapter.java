package com.carrotsearch.ant.tasks.junit4.listeners.json;

import java.lang.reflect.Type;

import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;
import com.google.gson.*;

/**
 * Serialization of {@link FailureMirror}. 
 */
public class JsonFailureMirrorAdapter implements JsonSerializer<FailureMirror> {
  @Override
  public JsonElement serialize(FailureMirror e, 
      Type type, JsonSerializationContext context) {
    JsonObject object = new JsonObject();
    object.addProperty("throwableClass", e.getThrowableClass());
    object.addProperty("throwableString", e.getThrowableString());
    object.addProperty("stackTrace", e.getTrace());
    
    object.addProperty("assertionViolation", e.isAssertionViolation());
    object.addProperty("assumptionViolation", e.isAssumptionViolation());
    object.addProperty("errorViolation", e.isErrorViolation());

    return object;
  }
}
