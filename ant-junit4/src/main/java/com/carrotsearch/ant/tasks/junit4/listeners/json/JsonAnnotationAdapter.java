package com.carrotsearch.ant.tasks.junit4.listeners.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.google.gson.*;

public class JsonAnnotationAdapter implements JsonSerializer<Annotation> {
  @Override
  public JsonElement serialize(Annotation src, Type typeOfSrc, JsonSerializationContext context) {
    Class<? extends Annotation> annType = src.annotationType();
    JsonObject ob = new JsonObject();
    for (Method m : annType.getDeclaredMethods()) {
      try {
        ob.add(m.getName(), context.serialize(m.invoke(src)));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    
    JsonObject annOb = new JsonObject();
    annOb.add(annType.getName(), ob);
    return annOb;
  }
}