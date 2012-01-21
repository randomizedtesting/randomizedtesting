package com.carrotsearch.maven.plugins.junit4;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.Assert;

import com.google.common.io.Files;

/**
 * Asserts on the content of integration tests' log file.
 */
public class LogAssert {
  private String logFile;

  public LogAssert(File buildLog) throws IOException {
    this.logFile = Files.toString(buildLog, Charset.defaultCharset());
  }

  public void assertLogContains(String text) {
    Assert.assertTrue("Log file was expected to contain: '"
        + text + "'", logFile.indexOf(text) >= 0);
  }
}
