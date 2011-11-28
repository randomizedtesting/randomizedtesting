package com.carrotsearch.ant.tasks.junit4.listeners.json;

import java.lang.reflect.Type;

import com.carrotsearch.ant.tasks.junit4.SlaveInfo;
import com.google.gson.*;

/**
 * Serialization of {@link SlaveInfo}. 
 */
public class JsonSlaveInfoAdapter implements JsonSerializer<SlaveInfo> {
  @Override
  public JsonElement serialize(SlaveInfo e, 
      Type type, JsonSerializationContext context) {
    JsonObject object = new JsonObject();
    object.addProperty("id", e.id);
    object.addProperty("jvmName", e.getJvmName());
    object.addProperty("charset", e.getCharset().displayName());
    object.add("systemProperties", context.serialize(e.getSystemProperties()));

    return object;
  }
}
