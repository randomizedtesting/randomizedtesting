package com.carrotsearch.ant.tasks.junit4.events.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import org.junit.runner.Description;

import com.google.common.collect.*;
import com.google.gson.*;

/**
 * Serialization and deserialization of {@link Description} instances. 
 */
public class JsonDescriptionAdapter implements JsonSerializer<Description>, JsonDeserializer<Description> {
  private static class ComparableDescription {
    final String id;
    final Description description;

    public ComparableDescription(Description description) {
      this.description = description;
      this.id = createId(description);
    }

    private String createId(Description description) {
      // TODO: We include annotation count, but not their content. Is this wrong?
      StringBuilder builder = new StringBuilder();
      builder.append("id#")
             .append(description.getDisplayName());
      builder.append("[")
             .append(description.getAnnotations().size())
             .append("]");
      return builder.toString();
    }

    @Override
    public int hashCode() {
      return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ComparableDescription) {
        return id.equals(((ComparableDescription) obj).id);
      } else {
        return false;
      }
    }
  }

  private final BiMap<ComparableDescription, String> identifiers;
  private final BiMap<String, ComparableDescription> identifiersInverse;
  
  public JsonDescriptionAdapter() {
    identifiers = HashBiMap.create();
    identifiersInverse = identifiers.inverse();
  }

  @Override
  public JsonElement serialize(Description e, Type type, JsonSerializationContext context) {
    final ComparableDescription key = new ComparableDescription(e);

    String id = identifiers.get(key);
    if (id != null) {
      return new JsonPrimitive(id);
    } else {
      id = key.id;
      identifiers.put(key, key.id);

      JsonObject object = new JsonObject();
      object.addProperty("id", id);
      object.addProperty("displayName", e.getDisplayName());
      object.addProperty("methodName", e.getMethodName());
      object.addProperty("className", e.getClassName());
      object.add("annotations", context.serialize(e.getAnnotations()));
      object.add("children", context.serialize(e.getChildren()));
      return object;
    }
  }

  @Override
  public Description deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {

    // Check if it's a full description or resolve backreference.
    if (json.isJsonPrimitive()) {
      String id = json.getAsString();
      ComparableDescription key = identifiersInverse.get(id);
      if (key == null) {
        throw new JsonParseException("No such reference: " + id);
      }
      return key.description;
    } else {
      JsonObject o = json.getAsJsonObject();
  
      List<Annotation> annotations = Lists.newArrayList();
      for (JsonElement child : o.getAsJsonArray("annotations")) {
        annotations.add((Annotation) context.deserialize(child, Annotation.class));
      }
  
      String displayName = o.getAsJsonPrimitive("displayName").getAsString();
      Description description = Description.createSuiteDescription(displayName, 
          annotations.toArray(new Annotation[annotations.size()]));
  
      for (JsonElement child : o.getAsJsonArray("children")) {
        description.addChild(deserialize(child, typeOfT, context));
      }

      ComparableDescription key = new ComparableDescription(description);
      identifiers.put(key, key.id); 
      return description;
    }
  }
}
