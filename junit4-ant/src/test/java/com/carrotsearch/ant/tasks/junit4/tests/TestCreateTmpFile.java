package com.carrotsearch.ant.tasks.junit4.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class TestCreateTmpFile {
  @Test
  public void createTmpFile() throws IOException {
    Path path = Paths.get(System.getProperty("java.io.tmpdir")).toAbsolutePath();
    System.out.println("Temporary folder at: " + path + ", exists: " + Files.exists(path));
    Path tmpFile = Files.createTempFile("tmp", ".txt");
    assertTrue(Files.exists(tmpFile));
    System.out.println("Created tmpfile: " + tmpFile.toAbsolutePath());
  }
}
