package com.carrotsearch.ant.tasks.junit4.listeners.json;

import java.lang.reflect.Type;

import com.google.gson.*;

public class JsonClassAdapter implements JsonSerializer<Class<?>> {
  @Override
  public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(src.getName()); 
  }
}