package com.carrotsearch.ant.tasks.junit4.listeners.antxml;

import org.simpleframework.xml.*;

import com.carrotsearch.ant.tasks.junit4.TestsSummary;

@Root(name = "failsafe-summary")
@Order(elements = {"completed", "errors", "failures", "skipped", "failureMessage"})
public class MavenFailsafeSummaryModel {
  private static final int FAILURE  = 255;
  private static final int NO_TESTS = 254;

  @Attribute(required = false)
  public Integer result;

  @Attribute
  public boolean timeout = false;

  @Element
  public int completed;

  @Element
  public int errors;

  @Element
  public int failures;

  @Element
  public int skipped;

  @Element(required = false)
  public String failureMessage = "";

  MavenFailsafeSummaryModel() {
  }

  public MavenFailsafeSummaryModel(TestsSummary result) {
    this.completed = result.tests;
    this.errors    = result.errors + result.suiteErrors;
    this.failures  = result.failures;
    this.skipped   = result.ignores + result.assumptions;
    
    if (!result.isSuccessful()) {
      this.result = FAILURE;
    } else if (result.tests == 0) {
      this.result = NO_TESTS;
    } else {
      this.result = /* OMIT_IN_OUTPUT */ null;
    }
  }
}