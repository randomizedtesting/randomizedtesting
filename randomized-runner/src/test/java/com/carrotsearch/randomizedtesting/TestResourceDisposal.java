package com.carrotsearch.randomizedtesting;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.junit.Assert;


/**
 * Check resource disposal facilities.
 */
public class TestResourceDisposal extends WithNestedTestClass {
  static class DummyCloseable implements Closeable {
    public boolean closed;
    public void close() throws IOException {
      if (closed) throw new IOException();
      closed = true;
    }
  }

  public static class Nested extends RandomizedTest {
    static List<DummyCloseable> testScope = new ArrayList<DummyCloseable>();
    static List<DummyCloseable> suiteScope = new ArrayList<DummyCloseable>();
    static boolean allTestScopeClosed;
    static boolean allSuiteScopeOpen;

    @BeforeClass
    public static void clean() {
      testScope.clear();
      suiteScope.clear();

      suiteScope.add(closeAfterSuite(new DummyCloseable()));
    }

    @AfterClass
    public static void afterClass() {
      allTestScopeClosed = true;
      for (DummyCloseable c : Nested.testScope) {
        if (!c.closed) allTestScopeClosed = false;
      }

      allSuiteScopeOpen = true;
      for (DummyCloseable c : Nested.suiteScope) {
        if (c.closed) allSuiteScopeOpen = false;
      }      
    }

    @Test
    public void testScopeResource() {
      assumeRunningNested();
      testScope.add(closeAfterTest(new DummyCloseable()));
      testScope.add(closeAfterTest(new DummyCloseable()));
    }
    
    @Test
    public void testScopeResourceWithFailures() {
      assumeRunningNested();
      testScope.add(closeAfterTest(new DummyCloseable()));
      testScope.add(closeAfterTest(new DummyCloseable()));
      throw new RuntimeException();
    }

    @Test
    public void suiteScopeFromTest() {
      assumeRunningNested();
      suiteScope.add(closeAfterSuite(new DummyCloseable()));
      suiteScope.add(closeAfterSuite(new DummyCloseable()));
    }    
  }

  @Test
  public void testResourceDisposalTestScope() {
    checkTestsOutput(3, 0, 1, 0, Nested.class);
    for (DummyCloseable c : Nested.testScope) {
      Assert.assertTrue(c.closed);
    }
    Assert.assertTrue(Nested.allTestScopeClosed);
  }

  @Test
  public void testResourceDisposalSuiteScope() {
    runTests(Nested.class);
    for (DummyCloseable c : Nested.suiteScope) {
      Assert.assertTrue(c.closed);
    }
    Assert.assertTrue(Nested.allSuiteScopeOpen);    
  }
  
  public static class Nested2 extends RandomizedTest {
    @Test
    public void testScope() {
      assumeRunningNested();
      closeAfterTest(closeAfterTest(new DummyCloseable()));
    }
  }

  @Test
  public void testFailedDisposalBreaksTestCase() {
    FullResult r = checkTestsOutput(1, 0, 1, 0, Nested2.class);
    Assert.assertTrue(r.getFailures().get(0).getException() instanceof ResourceDisposalError);
  }
}
