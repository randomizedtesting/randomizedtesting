package com.carrotsearch.ant.tasks.junit4.spikes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


public class CheckGSON {
  @RunWith(Suite.class)
  @SuiteClasses({
    Subclass1.class,
    Subclass2.class
  })
  public static class TestClass {
  }

  public static class Subclass1 {
    @Test
    public void testMethod() {}
  }

  public static class Subclass2 extends Subclass1 {
  }
  
  public static class ClassAdapter implements JsonSerializer<Class<?>> {
    @Override
    public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.getName()); 
    }
  }

  public static class AnnotationAdapter implements JsonSerializer<Annotation> {
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

  public static void main(String[] args) {
    JUnitCore core = new JUnitCore();
    core.addListener(new RunListener() {
      
      @Override
      public void testRunStarted(Description description) throws Exception {
      }

      @Override
      public void testStarted(Description description) throws Exception {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(Class.class, new ClassAdapter());
        builder.registerTypeHierarchyAdapter(Annotation.class, new AnnotationAdapter());
        Gson gson = builder.create();

        System.out.println(gson.toJson(description));
      }
    });
    Result r = core.run(TestClass.class);
    System.out.println(r);
    for (Failure f : r.getFailures()) {
      System.out.println(f.getTrace());
    }
  }
}
