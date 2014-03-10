package com.carrotsearch.randomizedtesting;

import static com.carrotsearch.randomizedtesting.annotations.TestGroup.Utilities.getSysProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.fest.assertions.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.annotations.TestGroup;
import com.carrotsearch.randomizedtesting.rules.SystemPropertiesRestoreRule;

/**
 * Custom test groups.
 */
public class TestTestFiltering extends WithNestedTestClass {
  @Rule
  public SystemPropertiesRestoreRule restoreProperties = new SystemPropertiesRestoreRule(); 

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Inherited
  @TestGroup(enabled = false)
  public static @interface Foo {
  }

  public static class Nested1 extends RandomizedTest {
    @Test @Foo
    public void test1() {
    }
  }

  @Test
  public void filterVsRulePriority() {
    System.setProperty(getSysProperty(Foo.class), "false");

    // Run @foo methods even though the group is disabled (by filtering rule).
    System.setProperty(SysGlobals.SYSPROP_TESTFILTER(), "@foo");
    checkResult(JUnitCore.runClasses(Nested1.class), 1, 0, 0);

    // Don't run by default.
    System.setProperty(SysGlobals.SYSPROP_TESTFILTER(), "");
    checkResult(JUnitCore.runClasses(Nested1.class), 1, 1, 0);
    
    // Run on default filter.
    System.setProperty(SysGlobals.SYSPROP_TESTFILTER(), "default");
    checkResult(JUnitCore.runClasses(Nested1.class), 0, 0, 0);        
  }

  private void checkResult(Result result, int run, int ignored, int failures) {
    Assertions.assertThat(result.getRunCount()).as("run count").isEqualTo(run);
    Assertions.assertThat(result.getIgnoreCount()).as("ignore count").isEqualTo(ignored);
    Assertions.assertThat(result.getFailureCount()).as("failure count").isEqualTo(failures);
  }
}
