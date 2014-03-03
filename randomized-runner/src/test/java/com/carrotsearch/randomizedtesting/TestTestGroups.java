package com.carrotsearch.randomizedtesting;

import java.lang.annotation.*;

import org.fest.assertions.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.annotations.Nightly;
import com.carrotsearch.randomizedtesting.annotations.TestGroup;

import static com.carrotsearch.randomizedtesting.annotations.TestGroup.Utilities.*;

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
  }

  @Group1 @Group2
  public static class Nested2 extends RandomizedTest {
    @Test
    public void test1() {
    }
  }

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
      checkResult(JUnitCore.runClasses(Nested1.class), 1, 1, 0);
      
      System.setProperty(group1Property, "true");
      checkResult(JUnitCore.runClasses(Nested1.class), 1, 1, 0);

      System.setProperty(group2Property, "true");
      checkResult(JUnitCore.runClasses(Nested1.class), 1, 0, 0);

      System.setProperty(group1Property, "false");      
      checkResult(JUnitCore.runClasses(Nested1.class), 1, 1, 0);
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
      checkResult(JUnitCore.runClasses(Nested2.class), 1, 1, 0);
      
      System.setProperty(group1Property, "true");
      checkResult(JUnitCore.runClasses(Nested2.class), 1, 1, 0);

      System.setProperty(group2Property, "true");
      checkResult(JUnitCore.runClasses(Nested2.class), 1, 0, 0);

      System.setProperty(group1Property, "false");      
      checkResult(JUnitCore.runClasses(Nested2.class), 1, 1, 0);
    } finally {
      System.clearProperty(group1Property);
      System.clearProperty(group2Property);
    }
  }

  private void checkResult(Result result, int run, int ignored, int failures) {
    Assertions.assertThat(result.getRunCount()).as("run count").isEqualTo(run);
    Assertions.assertThat(result.getIgnoreCount()).as("ignore count").isEqualTo(ignored);
    Assertions.assertThat(result.getFailureCount()).as("failure count").isEqualTo(failures);
  }
}
