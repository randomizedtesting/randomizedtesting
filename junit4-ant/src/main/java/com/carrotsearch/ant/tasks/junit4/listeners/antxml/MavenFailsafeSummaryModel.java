package com.carrotsearch.ant.tasks.junit4.listeners.antxml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import com.carrotsearch.ant.tasks.junit4.TestsSummary;

@Root(name = "failsafe-summary")
public class MavenFailsafeSummaryModel {
  private static final int FAILURE  = 255;
  private static final int NO_TESTS = 254;

  @Attribute(required = false)
  public Integer result;

  @Attribute
  public boolean timeout = false;

  @Attribute
  public int completed;

  @Attribute
  public int errors;

  @Attribute
  public int failures;

  @Attribute
  public int skipped;

  @Attribute
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