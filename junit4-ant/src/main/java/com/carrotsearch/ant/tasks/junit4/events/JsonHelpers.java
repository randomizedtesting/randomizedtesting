package com.carrotsearch.ant.tasks.junit4.events;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonReader;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonToken;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonWriter;
import com.google.common.base.Objects;

public final class JsonHelpers {
  public static void writeDescription(JsonWriter writer, Description e) throws IOException {
    String key = createId(e);
    if (writer.inContext(key)) {
      writer.value(key);
    } else {
      writer.registerInContext(key, e);
      writer.beginObject();
      writer.name("id").value(key);
      writer.name("displayName").value(e.getDisplayName());
      writer.name("methodName").value(e.getMethodName());
      writer.name("className").value(e.getClassName());
  
      writer.name("children").beginArray();
      for (Description child : e.getChildren()) {
        writeDescription(writer, child);
      }
      writer.endArray();
      writer.endObject();
    }
  }

  protected static Description readDescription(JsonReader reader) throws IOException {
    final Description description;
    if (reader.peek() == JsonToken.STRING) {
      String key = reader.nextString();
      description = (Description) reader.lookupInContext(key);
      if (description == null) {
        throw new IOException("Missing reference to: " + key);
      }
    } else {
      reader.beginObject();
      String key = AbstractEvent.readStringOrNullProperty(reader, "id");
      String displayName = AbstractEvent.readStringOrNullProperty(reader, "displayName");
      String methodName = AbstractEvent.readStringOrNullProperty(reader, "methodName");
      String className = AbstractEvent.readStringOrNullProperty(reader, "className");
    
      List<Description> children = new ArrayList<>();
      AbstractEvent.expectProperty(reader, "children").beginArray();
      while (reader.peek() != JsonToken.END_ARRAY) {
        children.add(readDescription(reader));
      }
      reader.endArray();

      description = Description.createSuiteDescription(displayName, new Annotation [] {});

      for (Description child : children) {
        description.addChild(child);
      }
      
      if (!Objects.equal(description.getMethodName(), methodName)) {
        throw new IOException(String.format(Locale.ROOT,
            "Insane, methodName does not match: %s, %s", description.getMethodName(), methodName));
      }
    
      if (!Objects.equal(description.getClassName(), className)) {
        throw new IOException(String.format(Locale.ROOT,
            "Insane, className does not match: %s, %s", description.getClassName(), className));
      }      
      
      reader.registerInContext(key, description);
      reader.endObject();
    } 
  
    return description;
  }

  private static String createId(Description description) {
    return "ID#" + description.getDisplayName();
  }
}
