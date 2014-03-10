package com.carrotsearch.ant.tasks.junit4.tests;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.TestGroup;

public class TestFilteringExpressions {
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Inherited
  @TestGroup(enabled = false)
  public static @interface Foo {}

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Inherited
  @TestGroup(enabled = false)
  public static @interface Bar {}
  
  public static class Example1 extends RandomizedTest {
    @Test @Foo
    public void testFoo() {
      System.out.println(">foo<");
    }
    
    @Test @Foo @Bar
    public void testFooBar() {
      System.out.println(">foobar<");
    }
    
    @Test @Bar
    public void testBar() {
      System.out.println(">bar<");
    }    
  }
  
  public static class Example2 extends RandomizedTest {
    @Test @Foo @Bar
    public void testFooBar() {
      System.out.println(">foobar2<");
    }
    
    @Test @Bar
    public void testBar() {
      System.out.println(">bar2<");
    }    
  }  
}
