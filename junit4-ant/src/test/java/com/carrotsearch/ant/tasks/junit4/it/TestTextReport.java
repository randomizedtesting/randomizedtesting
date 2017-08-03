package com.carrotsearch.ant.tasks.junit4.it;


import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.ant.tasks.junit4.tests.FailInAfterClass;
import com.carrotsearch.ant.tasks.junit4.tests.ReasonForAssumptionIgnored;

/**
 * Test report-text listener.
 */
public class TestTextReport extends JUnit4XmlTestBase {
  @Test 
  public void suiteerror() {
    super.executeTarget("suiteerror");
    
    int count = countPattern(getLog(), FailInAfterClass.MESSAGE);
    Assert.assertEquals(1, count);
  }

  @Test 
  public void reasonForIgnored() {
    super.executeTarget("reasonForIgnored");
    assertLogContains("@DisabledGroup");
    assertLogContains("> Cause: Annotated @Ignore");
    assertLogContains("(Ignored method.)");
  }

  @Test 
  public void reasonForIgnoredByDisabledGroup() {
    super.executeTarget("reasonForIgnoredByDisabledGroup");
    String log = getLog();
    Assert.assertTrue(log.contains("(@DisabledGroup(value=foo bar))") ||
                      log.contains("(@DisabledGroup(value=\"foo bar\"))"));
  }

	@Test 
	public void reasonForSuiteAssumptionIgnored() {
	  super.executeTarget("reasonForSuiteAssumptionIgnored");

	  int count = countPattern(getLog(), ReasonForAssumptionIgnored.MESSAGE);
    Assert.assertEquals(2, count);
	}

  @Test 
  public void listeners() {
    super.executeTarget("listeners");
    assertLogContains("testStarted: passing(com.carrotsearch.ant.tasks.junit4.tests.SuiteListeners)");
    assertLogContains("testFinished: passing(com.carrotsearch.ant.tasks.junit4.tests.SuiteListeners)");
  }

  @Test 
  public void timestamps() {
    super.executeTarget("timestamps");
    Assert.assertTrue(getLog(),
        Pattern.compile("\\[([0-9]{2}):([0-9]{2}):([0-9]{2})\\.([0-9]{3})\\]").matcher(getLog()).find());
  }
  
  @Test 
  public void sysoutsOnSuiteFailure() {
    super.executeTarget("sysoutsOnSuiteFailure");
    assertLogContains("ignored-sysout");
    assertLogContains("success-sysout");
    assertLogContains("afterclass-sysout");
    assertLogContains("beforeclass-sysout");
    super.restoreSyserr.println(getLog());
  }
  
  @Test 
  public void sysoutsOnSuiteTimeout() {
    super.executeTarget("sysoutsOnSuiteTimeout");
    assertLogContains("beforeclass-sysout");
    assertLogContains("test-sysout");
    String log = getLog();
    Assert.assertTrue(
        log.indexOf("1> test-sysout") <
        log.indexOf("Suite execution timed out:"));
  }

  @Test 
  public void sysoutsPassthrough() {
    super.executeTarget("sysouts_passthrough");
  }

  @Test 
  public void failureslist() {
    super.executeTarget("failureslist");
    assertLogContains("Tests with failures");
  }

  @Test 
  public void filtertrace_default() {
    super.executeTarget("filtertrace_default");

    assertLogDoesNotContain("at sun.reflect.");
    assertLogDoesNotContain("at java.lang.reflect.Method");
    assertLogDoesNotContain("at org.junit.runners.");
    assertLogDoesNotContain("at com.carrotsearch.ant.tasks.junit4.slave.SlaveMain");
  }
  
  @Test 
  public void filtertrace_custom() {
    super.executeTarget("filtertrace_custom");

    assertLogContains("java.lang.reflect.Method");
    assertLogDoesNotContain("at org.junit.");
    assertLogDoesNotContain(".SlaveMain.");
  }
  
  @Test 
  public void errorsSoFarIndicator() {
    super.executeTarget("errorsSoFar");
    assertLogContains("Completed [1/1 (1!)]");
  }  
}
