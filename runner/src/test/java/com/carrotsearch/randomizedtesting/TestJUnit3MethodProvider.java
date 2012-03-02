package com.carrotsearch.randomizedtesting;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runners.model.InitializationError;

import com.carrotsearch.randomizedtesting.annotations.TestMethodProviders;

public class TestJUnit3MethodProvider {
  @TestMethodProviders({JUnit3MethodProvider.class})
  public static class Base {}

  @SuppressWarnings("unused")
  public static class T4 extends Base {         private   void test1() {} }
  public static class T2 extends Base {                   void test1() {} }
  public static class T3 extends Base {         protected void test1() {} }
  public static class T1 extends Base {         public    void test1() {} }

  @SuppressWarnings("unused")
  public static class ST4 extends Base { static private   void test1() {} }
  public static class ST2 extends Base { static           void test1() {} }
  public static class ST3 extends Base { static protected void test1() {} }
  public static class ST1 extends Base { static public    void test1() {} }

  public static class AT1 extends Base {        public    void test1(int arg) {} }
  public static class RT1 extends Base {        public    int  test1() { return 0; } }

  @Test
  public void testJUnit3Valid() throws Exception {
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
  public void testJUnit3Invalid() {
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
  
  public static class S1 extends Base {         public    void test1() {} }
  public static class S2 extends S1   {         public    void test1() {} }
  public static class S3 extends S2   {         public    void test1() {} }

  @Test
  public void testJUnit3Overrides() throws Exception {
    Result r = new JUnitCore().run(new RandomizedRunner(S3.class));
    Assert.assertEquals(0, r.getFailureCount());
    Assert.assertEquals(1, r.getRunCount());
  }  
}
