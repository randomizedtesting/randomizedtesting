package com.carrotsearch.ant.tasks.junit4.listeners.json;

import java.lang.reflect.Type;

import org.junit.runner.Description;

import com.google.gson.*;

/**
 * Serialization of {@link Description}. 
 */
public class JsonDescriptionAdapter implements JsonSerializer<Description> {
  @Override
  public JsonElement serialize(Description e, 
      Type type, JsonSerializationContext context) {
    JsonObject object = new JsonObject();
    object.addProperty("displayName", e.getDisplayName());
    object.addProperty("methodName", e.getMethodName());
    object.addProperty("className", e.getClassName());
    object.add("annotations", context.serialize(e.getAnnotations()));
    object.add("children", context.serialize(e.getChildren()));
    return object;
  }
}
