package com.carrotsearch.randomizedtesting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.notification.Failure;
import org.junit.Assert;

public class TestUncaughtExceptionsDuplicated extends WithNestedTestClass {
  public static class Nested1 extends RandomizedTest {
    @BeforeClass
    public static void beforeClass() throws Exception {
      assumeRunningNested();
      Thread t = new Thread() {
        public void run() {
          throw new RuntimeException("foobar");
        }
      };
      t.start();
      t.join();
    }

    @Test
    public void test() {}
  }

  public static class Nested2 extends RandomizedTest {
    @BeforeClass
    public static void beforeClass() {
      assumeRunningNested();
    }

    @Test
    public void test1() throws Exception {
      Thread t = new Thread() {
        public void run() {
          throw new RuntimeException("foobar1");
        }
      };
      t.start();
      t.join();
    }

    @Test
    public void test2() throws Exception {
      Thread t = new Thread() {
        public void run() {
          throw new RuntimeException("foobar2");
        }
      };
      t.start();
      t.join();
    }
    
    @Test
    public void test3() throws Exception {
      Thread t = new Thread() {
        public void run() {
          throw new RuntimeException("foobar3");
        }
      };
      t.start();
      t.join();
    }    
  }

  public static class Nested3 extends RandomizedTest {
    @Before
    public void runBeforeTest() throws Exception {
      assumeRunningNested();
      Thread t = new Thread() {
        public void run() {
          throw new RuntimeException("foobar");
        }
      };
      t.start();
      t.join();
    }

    @Test
    public void test1() throws Exception {
    }
  }

  @Test
  public void testExceptionInBeforeClassFailsTheTest() {
    FullResult r = checkTestsOutput(1, 0, 1, 0, Nested1.class);
    Assert.assertTrue(r.getFailures().get(0).getTrace().contains("foobar"));
  }

  @Test
  public void testExceptionWithinTestFailsTheTest() {
    FullResult r = checkTestsOutput(3, 0, 3, 0, Nested2.class);

    ArrayList<String> foobars = new ArrayList<String>();
    for (Failure f : r.getFailures()) {
      Matcher m = Pattern.compile("foobar[0-9]+").matcher(f.getTrace());
      while (m.find()) {
        foobars.add(m.group());
      }
    }

    Collections.sort(foobars);
    Assert.assertEquals("[foobar1, foobar2, foobar3]", 
        Arrays.toString(foobars.toArray()));
  }

  @Test
  public void testExceptionWithinBeforeFailsTheTest() {
    FullResult r = checkTestsOutput(1, 0, 1, 0, Nested3.class);
    Assert.assertTrue(r.getFailures().get(0).getTrace().contains("foobar"));
  }
}
