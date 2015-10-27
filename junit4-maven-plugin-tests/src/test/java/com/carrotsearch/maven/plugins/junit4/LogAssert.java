package com.carrotsearch.maven.plugins.junit4;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.junit.Assert;

/**
 * Asserts on the content of integration tests' log file.
 */
public class LogAssert {
  private String logFile;

  public LogAssert(File buildLog) throws IOException {
    this.logFile = new String(Files.readAllBytes(buildLog.toPath()), Charset.defaultCharset());
  }

  public void assertLogContains(String text) {
    Assert.assertTrue("Log file was expected to contain: '"
        + text + "'", logFile.indexOf(text) >= 0);
  }
}
