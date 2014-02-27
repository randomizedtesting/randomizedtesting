package com.carrotsearch.ant.tasks.junit4.listeners.json;

import java.lang.reflect.Type;

import com.carrotsearch.ant.tasks.junit4.ForkedJvmInfo;
import com.google.gson.*;

/**
 * Serialization of {@link ForkedJvmInfo}. 
 */
public class JsonSlaveInfoAdapter implements JsonSerializer<ForkedJvmInfo> {
  @Override
  public JsonElement serialize(ForkedJvmInfo e, 
      Type type, JsonSerializationContext context) {
    JsonObject object = new JsonObject();
    object.addProperty("id", e.id);
    object.addProperty("jvmName", e.getJvmName());
    object.addProperty("charset", e.getCharset().displayName());
    object.addProperty("commandLine", e.getCommandLine());
    object.add("systemProperties", context.serialize(e.getSystemProperties()));

    return object;
  }
}
