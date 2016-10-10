package com.carrotsearch.randomizedtesting;

import static com.carrotsearch.randomizedtesting.annotations.TestGroup.Utilities.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.annotations.Nightly;
import com.carrotsearch.randomizedtesting.annotations.TestGroup;

/**
 * Custom test groups.
 */
public class TestTestGroups extends WithNestedTestClass {
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Inherited
  @TestGroup(enabled = true)
  public static @interface Group1 {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Inherited
  @TestGroup(enabled = false, name = "abc", sysProperty = "custom.abc")
  public static @interface Group2 {
  }

  public static class Nested1 extends RandomizedTest {
    @Test @Group1 @Group2
    public void test1() {
    }
    
    @BeforeClass
    public static void beforeClass() {
      beforeClassRan = true;
    }
    
    @AfterClass
    public static void afterClass() {
      afterClassRan = true;
    }
  }

  public static class Nested3 extends Nested1 {
    @Test
    public void testUnconditional() {
    }
  }

  @Group1 @Group2
  public static class Nested2 extends RandomizedTest {
    @Test
    public void test1() {
    }
    
    @BeforeClass
    public static void beforeClass() {
      beforeClassRan = true;
    }
    
    @AfterClass
    public static void afterClass() {
      afterClassRan = true;
    }    
  }
  
  public static boolean beforeClassRan;
  public static boolean afterClassRan;
  
  @Test
  public void checkDefaultNames() {
    Assert.assertEquals("group1", getGroupName(Group1.class));
    Assert.assertEquals("abc", getGroupName(Group2.class));
    Assert.assertEquals(SysGlobals.CURRENT_PREFIX() + ".group1", getSysProperty(Group1.class));
    Assert.assertEquals("custom.abc", getSysProperty(Group2.class));
    Assert.assertEquals(SysGlobals.CURRENT_PREFIX() + ".nightly", getSysProperty(Nightly.class));
    Assert.assertEquals("nightly", getGroupName(Nightly.class));
  }  

  @Test
  public void groupsOnMethods() {
    String group1Property = getSysProperty(Group1.class);
    String group2Property = getSysProperty(Group2.class);
    try {
      afterClassRan = beforeClassRan = false;
      checkTestsOutput(1, 0, 0, 1, Nested1.class);
      Assert.assertFalse(afterClassRan);
      Assert.assertFalse(beforeClassRan);
      
      afterClassRan = beforeClassRan = false;
      System.setProperty(group1Property, "true");
      checkTestsOutput(1, 0, 0, 1, Nested1.class);
      Assert.assertFalse(afterClassRan);
      Assert.assertFalse(beforeClassRan);

      afterClassRan = beforeClassRan = false;
      System.setProperty(group2Property, "true");
      checkTestsOutput(1, 0, 0, 0, Nested1.class);
      Assert.assertTrue(afterClassRan);
      Assert.assertTrue(beforeClassRan);

      afterClassRan = beforeClassRan = false;
      System.setProperty(group1Property, "false");
      checkTestsOutput(1, 0, 0, 1, Nested1.class);
      Assert.assertFalse(afterClassRan);
      Assert.assertFalse(beforeClassRan);
    } finally {
      System.clearProperty(group1Property);
      System.clearProperty(group2Property);
    }
  }

  @Test
  public void groupsOnASubsetOfMethods() {
    String group1Property = getSysProperty(Group1.class);
    String group2Property = getSysProperty(Group2.class);
    try {
      afterClassRan = beforeClassRan = false;
      checkTestsOutput(2, 0, 0, 1, Nested3.class);
      Assert.assertTrue(afterClassRan);
      Assert.assertTrue(beforeClassRan);
      
      afterClassRan = beforeClassRan = false;
      System.setProperty(group1Property, "true");
      checkTestsOutput(2, 0, 0, 1, Nested3.class);
      Assert.assertTrue(afterClassRan);
      Assert.assertTrue(beforeClassRan);

      afterClassRan = beforeClassRan = false;
      System.setProperty(group2Property, "true");
      checkTestsOutput(2, 0, 0, 0, Nested3.class);
      Assert.assertTrue(afterClassRan);
      Assert.assertTrue(beforeClassRan);

      afterClassRan = beforeClassRan = false;
      System.setProperty(group1Property, "false");
      checkTestsOutput(2, 0, 0, 1, Nested3.class);
      Assert.assertTrue(afterClassRan);
      Assert.assertTrue(beforeClassRan);
    } finally {
      System.clearProperty(group1Property);
      System.clearProperty(group2Property);
    }
  }

  @Test
  public void groupsOnClass() {
    String group1Property = getSysProperty(Group1.class);
    String group2Property = getSysProperty(Group2.class);
    try {
      afterClassRan = beforeClassRan = false;
      checkTestsOutput(1, 0, 0, 1, Nested2.class);
      Assert.assertFalse(afterClassRan);
      Assert.assertFalse(beforeClassRan);

      afterClassRan = beforeClassRan = false;
      System.setProperty(group1Property, "true");
      checkTestsOutput(1, 0, 0, 1, Nested2.class);
      Assert.assertFalse(afterClassRan);
      Assert.assertFalse(beforeClassRan);

      afterClassRan = beforeClassRan = false;
      System.setProperty(group2Property, "true");
      checkTestsOutput(1, 0, 0, 0, Nested2.class);
      Assert.assertTrue(afterClassRan);
      Assert.assertTrue(beforeClassRan);

      afterClassRan = beforeClassRan = false;
      System.setProperty(group1Property, "false");      
      checkTestsOutput(1, 0, 0, 1, Nested2.class);
      Assert.assertFalse(afterClassRan);
      Assert.assertFalse(beforeClassRan);
    } finally {
      System.clearProperty(group1Property);
      System.clearProperty(group2Property);
    }
  }
}
