package com.carrotsearch.randomizedtesting;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

public class TestGlobFilter {
  @Test
  public void testBrackets() {
    GlobFilter gf = new MethodGlobFilter("ab(*)");
    Assert.assertTrue(gf.globMatches("ab()"));
    Assert.assertTrue(gf.globMatches("ab(foo)"));
    Assert.assertTrue(gf.globMatches("ab(bar=xxx)"));

    gf = new MethodGlobFilter("test {yaml=resthandler/10_Foo/Bar (Hello)}");
    Assert.assertTrue(gf.globMatches("test {yaml=resthandler/10_Foo/Bar (Hello)}"));
  }
}
