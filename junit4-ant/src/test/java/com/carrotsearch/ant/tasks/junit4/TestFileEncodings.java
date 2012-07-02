package com.carrotsearch.ant.tasks.junit4;


import org.junit.Assert;
import org.junit.Test;


public class TestFileEncodings extends JUnit4XmlTestBase {
  @Test
  public void checkTypicalEncodings() {
    //super.executeTarget("fileencodings");
    super.executeForkedTarget("fileencodings");

    String log = getLog();
    Assert.assertEquals(1, countPattern(log, "US-ASCII=cze??, ??????, ???"));
    Assert.assertEquals(1, countPattern(log, "iso8859-1=cze??, ??????, ???"));
    Assert.assertEquals(1, countPattern(log, "UTF-8=cześć, Привет, 今日は"));
    Assert.assertEquals(1, countPattern(log, "UTF-16=cześć, Привет, 今日は"));
    Assert.assertEquals(1, countPattern(log, "UTF-16LE=cześć, Привет, 今日は"));
    Assert.assertEquals(1, countPattern(log, "UTF-32=cześć, Привет, 今日は"));
  }
}
