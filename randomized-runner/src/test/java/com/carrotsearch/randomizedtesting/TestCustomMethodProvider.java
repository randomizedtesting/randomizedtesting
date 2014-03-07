package com.carrotsearch.randomizedtesting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runners.model.InitializationError;

import com.carrotsearch.randomizedtesting.annotations.TestMethodProviders;

public class TestCustomMethodProvider extends WithNestedTestClass {
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  @Inherited
  public static @interface MyTest {
  }
  
  public static class MyTestMethodProvider extends AnnotatedMethodProvider {
    public MyTestMethodProvider() {
      super(MyTest.class);
    }
  }
  
  @TestMethodProviders({MyTestMethodProvider.class})
  public static class Base {}

  public static class T4 extends Base { @MyTest  private  void test1() {} }
  public static class T2 extends Base { @MyTest           void test1() {} }
  public static class T3 extends Base { @MyTest protected void test1() {} }
  public static class T1 extends Base { @MyTest   public  void test1() {} }

  public static class ST4 extends Base { @MyTest static private   void test1() {} }
  public static class ST2 extends Base { @MyTest static           void test1() {} }
  public static class ST3 extends Base { @MyTest static protected void test1() {} }
  public static class ST1 extends Base { @MyTest static   public  void test1() {} }
  
  public static class AT1 extends Base { @MyTest public void test1(int arg) {} }
  public static class RT1 extends Base { @MyTest public int  test1() { return 0; } }

  @Test
  public void testJUnit4Valid() throws InitializationError {
    Class<?> [] valid = {
        T1.class, RT1.class
    };

    for (Class<?> cl : valid) {
      Result r = new JUnitCore().run(new RandomizedRunner(cl));
      Assert.assertEquals(0, r.getFailureCount());
      Assert.assertEquals(1, r.getRunCount());
    }
  }
  
  @Test
  public void testJUnit4Invalid() {
    Class<?> [] invalid = {
        T2.class, T3.class, T4.class,
        ST1.class, ST2.class, ST3.class, ST4.class,
        AT1.class
    };

    for (Class<?> cl : invalid) {
      try {
        new JUnitCore().run(new RandomizedRunner(cl));
        Assert.fail("Expected to fail for: " + cl);
      } catch (InitializationError e) {
        // expected.
      }
    }
  }  
}
