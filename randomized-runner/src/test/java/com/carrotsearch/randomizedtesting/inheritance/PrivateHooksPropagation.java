package com.carrotsearch.randomizedtesting.inheritance;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.RandomizedTest;

public class PrivateHooksPropagation {
  final static List<String> order = new ArrayList<>();

  public static class Super extends RandomizedTest {
    @BeforeClass private   static void beforeClass1() { order.add("super.beforeclass1"); }
    @BeforeClass protected static void beforeClass2() { order.add("super.beforeclass2"); }

    @Before      private   void before1() { order.add("super.before1"); }
    @Before      protected void before2() { order.add("super.before2"); }

    @Test        public void testMethod1() { order.add("super.testMethod1"); }
  }

  public static class Sub extends Super {
    @BeforeClass private   static void beforeClass1() { order.add("sub.beforeclass1"); }
    @BeforeClass protected static void beforeClass2() { order.add("sub.beforeclass2"); }

    @Before      private   void before1() { order.add("sub.before1"); }
    @Before      protected void before2() { order.add("sub.before2"); }
  }

  @Test
  public void checkOldMethodRules() throws Exception {
    assertSameExecution(Sub.class);
  }

  private void assertSameExecution(Class<?> clazz) throws Exception {
    new JUnitCore().run(Request.runner(new RandomizedRunner(clazz)));
    List<String> order1 = new ArrayList<>(order);
    order.clear();

    String msg = "# RR order:\n" + order1;
    Assertions.assertThat(order1).as(msg).containsOnly(
        "super.beforeclass1",
        "sub.beforeclass1",
        "sub.beforeclass2",
        "super.before1",
        "sub.before1",
        "sub.before2",
        "super.testMethod1");
  }
}
