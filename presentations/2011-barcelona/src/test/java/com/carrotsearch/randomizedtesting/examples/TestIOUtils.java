package com.carrotsearch.randomizedtesting.examples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

public class TestIOUtils extends RandomizedTest {
  @Test
  @Repeat(iterations = 10)
  public void testReadFile() throws IOException {
    File tempFile = newTempFile();
    FileOutputStream fos = new FileOutputStream(tempFile);
    String contents = randomUnicodeString();
    fos.write(contents.getBytes(UTF8));
    fos.close();
    
    String readBack = IOUtils.readFile(tempFile, UTF8);
    Assert.assertEquals(contents, readBack);
  }

  @Test
  public void testLockFolder() throws IOException {
    File tempDir = newTempDir();
    FileOutputStream fos = new FileOutputStream(new File(tempDir,
        "testFile.txt"));
    fos.write("Hello Barcelona".getBytes(UTF8));
    // Don't close fos - this results in the parent folder locked (at least on
    // Windows).
    // fos.close();
  }
}
