package com.carrotsearch.ant.tasks.junit4.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

public class TestDir {
  @Test
  public void createDir() throws IOException {
    Files.createFile(Paths.get("touch.me"));
  }
}
