package com.carrotsearch.ant.tasks.junit4.it;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class TestFileEncodings extends JUnit4XmlTestBase {
  @Test
  public void checkTypicalEncodings() throws IOException {
    super.executeTarget("fileencodings");

    File logFile = new File(getProject().getProperty("log.file"));

    byte [] contents = new byte [(int) logFile.length()];
    DataInputStream dis = new DataInputStream(new FileInputStream(logFile));
    dis.readFully(contents);
    dis.close();
    
    String log = new String(contents, "UTF-8");
    Assert.assertEquals(1, countPattern(log, "US-ASCII=cze??, ??????, ???"));
    Assert.assertEquals(1, countPattern(log, "iso8859-1=cze??, ??????, ???"));
    Assert.assertEquals(1, countPattern(log, "UTF-8=cześć, Привет, 今日は"));
    Assert.assertEquals(1, countPattern(log, "UTF-16=cześć, Привет, 今日は"));
    Assert.assertEquals(1, countPattern(log, "UTF-16LE=cześć, Привет, 今日は"));
    Assert.assertEquals(1, countPattern(log, "UTF-32=cześć, Привет, 今日は"));
  }
}
