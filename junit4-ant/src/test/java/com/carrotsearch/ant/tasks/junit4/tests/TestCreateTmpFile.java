package com.carrotsearch.ant.tasks.junit4.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;

public class TestCreateTmpFile extends RandomizedTest {
  @Test
  public void createTmpFile() throws IOException {
    Path tmpFile = Files.createTempFile("tmp", ".txt");
    assertTrue(Files.exists(tmpFile));
    System.out.println("Created tmpfile: " + tmpFile.toAbsolutePath());
  }
}
