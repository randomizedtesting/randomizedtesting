package com.carrotsearch.randomizedtesting;

import com.carrotsearch.randomizedtesting.annotations.TestGroup;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestTestGroupsOnOverrides extends WithNestedTestClass {
  static List<String> buf;

  public static class Super extends RandomizedTest {
    @Before
    public void assumeNested() {
      assumeRunningNested();
    }

    @Test
    public void method() {
      buf.add("super:method");
    }
  }

  @Before
  public void clean() {
    buf = new ArrayList<>();
  }

  public static class Ignored extends Super {
    @Ignore
    public void method() {
      buf.add("sub:method");
    }
  }

  @Test
  public void testIgnored() {
    runTests(Ignored.class);
    assertThat(buf).isEmpty();
  }

  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Inherited
  @TestGroup(enabled = false)
  public @interface Disabled {
  }

  public static class DisabledGroup extends Super {
    @Disabled
    public void method() {
      buf.add("sub:method");
    }
  }

  @Test
  public void testDisabledGroup() {
    runTests(DisabledGroup.class);
    assertThat(buf).isEmpty();
  }

  public static class Normal extends Super {
    public void method() {
      buf.add("sub:method");
    }
  }

  @Test
  public void testNormal() {
    runTests(Normal.class);
    assertThat(buf).containsExactly("sub:method");
  }
}

