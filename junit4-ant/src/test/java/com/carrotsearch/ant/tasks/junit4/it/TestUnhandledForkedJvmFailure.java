package com.carrotsearch.ant.tasks.junit4.it;

import java.io.IOException;

import org.junit.Test;

import com.carrotsearch.ant.tasks.junit4.tests.FireUnhandledRunnerException;

public class TestUnhandledForkedJvmFailure extends JUnit4XmlTestBase {
  @Test
  public void checkForkedMainFailure() throws IOException {
    super.expectBuildExceptionContaining("forkedmainfailure", "process threw an exception");
    assertLogContains(FireUnhandledRunnerException.EXCEPTION_MESSAGE);
  }
}
