package com.carrotsearch.randomizedtesting;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.*;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

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
    Result runClasses = runClasses(Nested1.class);
    Assert.assertEquals(1, runClasses.getFailureCount());
    Assert.assertEquals(1, runClasses.getRunCount());
    Assert.assertTrue(runClasses.getFailures().get(0).getTrace().contains("foobar"));
  }

  @Test
  public void testExceptionWithinTestFailsTheTest() {
    Result runClasses = runClasses(Nested2.class);
    Assert.assertEquals(3, runClasses.getFailureCount());
    Assert.assertEquals(3, runClasses.getRunCount());

    ArrayList<String> foobars = new ArrayList<String>();
    for (Failure f : runClasses.getFailures()) {
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
    Result runClasses = runClasses(Nested3.class);
    Assert.assertEquals(1, runClasses.getFailureCount());
    Assert.assertEquals(1, runClasses.getRunCount());
    Assert.assertTrue(runClasses.getFailures().get(0).getTrace().contains("foobar"));
  }
}
