package com.carrotsearch.ant.tasks.junit4.events.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonAnnotationAdapter implements JsonSerializer<Annotation>, JsonDeserializer<Annotation> {
  private final ClassLoader refLoader;

  public JsonAnnotationAdapter(ClassLoader refLoader) {
    this.refLoader = refLoader;
  }

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

  @Override
  public Annotation deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    
    JsonObject o = json.getAsJsonObject();
    Set<Entry<String,JsonElement>> entrySet = o.entrySet();
    if (entrySet.size() != 1) {
      throw new JsonParseException("Annotation type with more than one property?");
    }

    Entry<String,JsonElement> kv = entrySet.iterator().next();
    String annClassName = kv.getKey();
    try {
      final Class<?> annClass = Class.forName(annClassName, false, refLoader);
      final Map<Method, Object> methods = Maps.newHashMap();
      for (Entry<String,JsonElement> e : kv.getValue().getAsJsonObject().entrySet()) {
        Method m = annClass.getMethod(e.getKey());
        methods.put(m, context.deserialize(e.getValue(), m.getGenericReturnType()));
      }

      final Method annotationTypeMethod = Annotation.class.getMethod("annotationType");
      InvocationHandler invHandler = new InvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
          if (method.equals(annotationTypeMethod)) {
            return annClass;
          } else if (methods.containsKey(method)) {
            return methods.get(method);
          }
          return method.getDefaultValue();
        }
      };

      return (Annotation) Proxy.newProxyInstance(refLoader, new Class [] { annClass }, invHandler);
    } catch (Exception e) {
      throw new JsonParseException(e);
    }
  }
}