package com.carrotsearch.ant.tasks.junit4.tests.obs;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class TestDir {
  @Test
  public void createDir() throws IOException {
    new File("touch.me").createNewFile();
  }
}
