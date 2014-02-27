package com.carrotsearch.ant.tasks.junit4.it;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;


public class TestReplication extends JUnit4XmlTestBase {
  @Test
  public void singleSuiteReplication() {
    super.executeTarget("replicateSingleTest");

    assertLogContains("Replicated test, VM: 0");
    assertLogContains("Replicated test, VM: 1");
    assertLogContains("Replicated test, VM: 2");
  }

  @Test
  public void replicationAndBalancing() {
    super.executeTarget("replicationAndBalancing");
    
    assertLogContains("Non-replicated test, VM: 0");
    assertLogContains("Non-replicated test, VM: 1");
    assertLogContains("Replicated test, VM: 0");
    assertLogContains("Replicated test, VM: 1");
    assertLogContains("Replicated test, VM: 2");
  }

  @Test
  public void pseudoBalancing() {
    super.executeTarget("pseudoBalancing");

    assertLogContains("3 suites, 300 tests, 200 ignored (200 assumptions)");

    String log = getLog();
    for (int i = 0; i < 100; i++) {
      Pattern p = Pattern.compile("(Test " + i + " executed on VM )([0-9]+)");
      Matcher m = p.matcher(log);
      Assert.assertTrue(m.find());
      int jvm = Integer.parseInt(m.group(2));
      
      for (int ignoredOnJvm : Sets.difference(Sets.newHashSet(0, 1, 2), Sets.newHashSet(jvm))) {
        assertLogContains("Test " + i + " ignored on VM " + ignoredOnJvm);
      }
    }
  }
}
