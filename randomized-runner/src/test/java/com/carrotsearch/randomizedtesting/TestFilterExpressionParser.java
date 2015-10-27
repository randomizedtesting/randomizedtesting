package com.carrotsearch.randomizedtesting;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.FilterExpressionParser.IContext;
import com.carrotsearch.randomizedtesting.FilterExpressionParser.Node;
import com.carrotsearch.randomizedtesting.FilterExpressionParser.SyntaxException;

public class TestFilterExpressionParser {
  @Test
  public void testParsingSanity() {
    // input -> expected bracketing.
    Deque<String> examples = new ArrayDeque<String>(Arrays.asList(
        "", "default",
        "default", "default",
        "@nightly", "@nightly",
        "@nightly or @slow", "(@nightly OR @slow)",
        "@nightly and @slow", "(@nightly AND @slow)",
        "@nightly and not @slow", "(@nightly AND (NOT @slow))",
        "@nightly and (not @slow)", "(@nightly AND (NOT @slow))",
        "not @slow", "(NOT @slow)",
        "not not @slow", "(NOT (NOT @slow))",
        "not @nightly or @slow", "((NOT @nightly) OR @slow)",
        "not(@nightly or @slow)", "(NOT (@nightly OR @slow))",
        "(@nightly and (not @slow))", "(@nightly AND (NOT @slow))"));

    while (!examples.isEmpty()) {
      String input = examples.remove();
      String expected = examples.remove();

      System.out.println("-- " + input);
      Node root = new FilterExpressionParser().parse(input);
      String expression = root.toExpression();
      System.out.println("   " + expression);

      Assertions.assertThat(expression)
        .as(input)
        .isEqualTo(expected);

      Assertions.assertThat(new FilterExpressionParser().parse(expression).toExpression())
        .as(input)
        .isEqualTo(expression);
    }
  }

  @Test
  public void testTrueExpressions() {
    // {groups}, {rules}
    Deque<String[]> examples = new ArrayDeque<String[]>(Arrays.asList(new String [][] {
        {},
        {"not @foo", "not default"},

        {"@nightly"},
        {"@nightly", "@nightly or @foo", "@foo or @nightly", "not not @nightly"},

        {"@nightly", "@slow"},
        {"@nightly and @slow", "@nightly and not @foo", "not @nightly or @slow"},

        {"default"},
        {"", "default"},        
    }));

    while (!examples.isEmpty()) {
      final List<String> groups = Arrays.asList((String[]) examples.pop());
      IContext context = new IContext() {
        @Override
        public boolean defaultValue() {
          return hasGroup("default");
        }

        @Override
        public boolean hasGroup(String value) {
          return groups.contains(value);
        }
      };

      for (String rule : examples.pop()) {
        Assertions.assertThat(new FilterExpressionParser().parse(rule).evaluate(context))
          .as("ctx=" + groups + ", rule=" + rule)
          .isEqualTo(true);
      }
    }
  }

  @Test
  public void testFalseExpressions() {
    // {groups}, {rules}
    Deque<String[]> examples = new ArrayDeque<String[]>(Arrays.asList(new String [][] {
        {},
        {"@foo", "default"},

        {"@nightly"},
        {"not @nightly", "@nightly and @foo"},

        {"@nightly", "@slow"},
        {"not(@nightly or @slow)"},
    }));

    while (!examples.isEmpty()) {
      final List<String> groups = Arrays.asList((String[]) examples.pop());
      IContext context = new IContext() {
        @Override
        public boolean defaultValue() {
          return hasGroup("default");
        }

        @Override
        public boolean hasGroup(String value) {
          return groups.contains(value);
        }
      };

      for (String rule : examples.pop()) {
        Assertions.assertThat(new FilterExpressionParser().parse(rule).evaluate(context))
          .as("ctx=" + groups + ", rule=" + rule)
          .isEqualTo(false);
      }
    }
  }

  @Test
  public void testErrors() {
    for (String rule : Arrays.asList(
        "nightly",
        "@nightly and ",
        "@nightly or ",
        "@nightly and or @slow",
        "and not @slow",
        "(@nightly))",
        "((@nightly or @slow)"
        )) {

      System.out.println("-- " + rule);
      try {
        new FilterExpressionParser().parse(rule);
        Assertions.fail("Should cause an exception: " + rule);
      } catch (SyntaxException e) {
        System.out.println("   " + e.getMessage());
      }
    }
  }
  
  @Test
  public void testInvalidToken() {
    try {
      new FilterExpressionParser().parse("@nightly and foo or @bar");
    } catch (SyntaxException e) {
      Assertions.assertThat(e.toString()).contains(">foo<");
    }
  }  
}
