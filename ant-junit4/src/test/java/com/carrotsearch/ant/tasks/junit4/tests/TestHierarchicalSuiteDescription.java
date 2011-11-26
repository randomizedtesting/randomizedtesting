package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  TestHierarchicalSuiteDescription.Sub1.class,
  TestHierarchicalSuiteDescription.Sub2.class,
  TestHierarchicalSuiteDescription.Sub3.class
})
public class TestHierarchicalSuiteDescription {
  public static class Sub1 {
    @Test
    public void method1() {}
    
    @BeforeClass
    public static void beforeClass() {
      throw new RuntimeException();
    }
  }
  
  public static class Sub2 {
    @Test
    public void method1() {}

    @AfterClass
    public static void afterClass() {
      throw new RuntimeException();
    }    
  }

  public static class Sub3 {
    @Test
    public void method1() {
      throw new RuntimeException();
    }
  }

  @AfterClass
  public static void afterClass() {
    throw new RuntimeException();
  }
}
