package com.carrotsearch.randomizedtesting;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.TestCaseOrdering;

public class TestTestCaseOrdering extends WithNestedTestClass {
  static List<String> buf;

  @TestCaseOrdering(TestCaseOrdering.AlphabeticOrder.class)
  public static class Alphabetical extends RandomizedTest {
    @Before public void assumeNested() { assumeRunningNested(); }
    @Test public void a() { buf.add("a"); }
    @Test public void b() { buf.add("b"); }
    @Test public void c() { buf.add("c"); }
    @Test public void d() { buf.add("d"); }
  }

  @TestCaseOrdering(TestCaseOrdering.AlphabeticOrder.class)
  @Repeat(iterations = 3)
  public static class AlphabeticalWithRepetitions extends RandomizedTest {
    @Before public void assumeNested() { assumeRunningNested(); }
    @Test public void a() { buf.add("a"); }
    @Test public void b() { buf.add("b"); }
    @Test public void c() { buf.add("c"); }
    @Test public void d() { buf.add("d"); }
  }

  public static class CustomOrder implements Comparator<TestMethodAndParams> {
    @Override
    public int compare(TestMethodAndParams o1, TestMethodAndParams o2) {
      int v = o1.getTestMethod().getName().compareTo(
              o2.getTestMethod().getName());
      if (v == 0) {
        v = ((String) o1.getInstanceArguments().get(0)).compareTo(
            ((String) o2.getInstanceArguments().get(0)));
      }

      return v;
    }
  }
  
  @TestCaseOrdering(CustomOrder.class)
  public static class AlphabeticalWithParameters extends RandomizedTest {
    String param;
    
    public AlphabeticalWithParameters(String param) {
      this.param = param;
    }

    @Before public void assumeNested() { assumeRunningNested(); }
    @Test public void a() { buf.add("a:" + param); }
    @Test public void b() { buf.add("b:" + param); }
    @Test public void c() { buf.add("c:" + param); }
    @Test public void d() { buf.add("d:" + param); }

    @ParametersFactory
    public static Iterable<Object[]> params() {
      return Arrays.asList($("1"), $("2"), $("3"));
    }
  }

  @Before
  public void clean() {
    buf = new ArrayList<>();
  }

  @Test
  public void testAlphabetical() {
    runTests(Alphabetical.class);
    assertThat(buf).containsExactly("a", "b", "c", "d");
  }

  @Test
  public void testAlphabeticalWithRepetitions() {
    runTests(AlphabeticalWithRepetitions.class);
    assertThat(buf).containsExactly(
        "a", "a", "a",
        "b", "b", "b",
        "c", "c", "c",
        "d", "d", "d");
  }

  @Test
  public void testAlphabeticalWithParameters() {
    runTests(AlphabeticalWithParameters.class);
    assertThat(buf).containsExactly(
        "a:1", "a:2", "a:3",
        "b:1", "b:2", "b:3",
        "c:1", "c:2", "c:3",
        "d:1", "d:2", "d:3");
  }  
}

