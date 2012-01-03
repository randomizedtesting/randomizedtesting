package com.carrotsearch.ant.tasks.junit4.events.json;

import java.lang.reflect.Type;

import com.google.gson.*;

public class JsonClassAdapter implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {
  private final ClassLoader refLoader;

  public JsonClassAdapter(ClassLoader refLoader) {
    this.refLoader = refLoader;
  }

  @Override
  public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(src.getName()); 
  }
  
  @Override
  public Class<?> deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    try {
      return Class.forName(json.getAsString(), false, refLoader);
    } catch (ClassNotFoundException e) {
      throw new JsonParseException(e);
    }
  }
}