package com.carrotsearch.randomizedtesting;

import static com.carrotsearch.randomizedtesting.annotations.TestGroup.Utilities.getSysProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;

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

  static AtomicInteger counter = new AtomicInteger();

  public static class Nested1 extends RandomizedTest {
    @Test @Foo
    public void test1() {
      counter.incrementAndGet();
    }
  }

  @Test
  public void filterVsRulePriority() {
    System.setProperty(getSysProperty(Foo.class), "false");

    // Don't run by default (group is disabled by default).
    counter.set(0);
    System.setProperty(SysGlobals.SYSPROP_TESTFILTER(), "");
    checkTestsOutput(1, 0, 0, 1, Nested1.class);
    Assertions.assertThat(counter.get()).isEqualTo(0);

    // Run @foo methods even though the group is disabled (but the filtering rule takes priority).
    counter.set(0);
    System.setProperty(SysGlobals.SYSPROP_TESTFILTER(), "@foo");
    checkTestsOutput(1, 0, 0, 0, Nested1.class);
    Assertions.assertThat(counter.get()).isEqualTo(1);

    // Run the "default" filter.
    counter.set(0);
    System.setProperty(SysGlobals.SYSPROP_TESTFILTER(), "default");
    checkTestsOutput(0, 0, 0, 0, Nested1.class);
    Assertions.assertThat(counter.get()).isEqualTo(0);
  }
}
