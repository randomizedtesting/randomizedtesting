package com.carrotsearch.randomizedtesting;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.Arrays;

import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;

import static org.junit.Assert.*;

public class TestRandomizedTest extends RandomizedTest {
  @Test
  public void testRandomIntBetween() {
    boolean [] array = new boolean [10];
    for (int i = 0; i < 10000; i++) 
      array[randomIntBetween(0, array.length - 1)] = true;

    for (boolean b: array) 
      assertTrue(b);
  }

  @Test
  public void testAtLeast() {
    assertEquals(atLeast(Integer.MAX_VALUE), Integer.MAX_VALUE);
    int v = randomIntBetween(0, Integer.MAX_VALUE);
    for (int i = 0; i < 10000; i++) 
      assertTrue(atLeast(v) >= v);
  }

  @Test
  public void testAtMost() {
    assertEquals(atMost(0), 0);
    int v = randomIntBetween(0, Integer.MAX_VALUE);
    for (int i = 0; i < 10000; i++) 
      assertTrue(atMost(v) <= v);
  }

  @Test
  public void testRandomIntBetweenBoundaryCases() {
    for (int i = 0; i < 10000; i++) {
      int j = randomIntBetween(0, Integer.MAX_VALUE);
      assertTrue(j >= 0 && j <= Integer.MAX_VALUE);

      // This must fall in range, but nonetheless
      randomIntBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
  }

  @Test
  public void testRandomFromArray() {
    try {
      randomFrom(new Object [] {});
      fail();
    } catch (IllegalArgumentException e) {
      // expected.
    }
    
    Integer [] ints = new Integer [10];
    for (int i = 0; i < ints.length; i++)
      ints[i] = i;
    
    for (int i = 0; i < 10000; i++) {
      Integer j = randomFrom(ints);
      if (j != null) {
        ints[j] = null;
      }
    }
    
    for (int i = 0; i < ints.length; i++)
      assertTrue(ints[i] == null);
  }

  @Test
  public void testRandomFromList() {
    try {
      randomFrom(Collections.emptyList());
      fail();
    } catch (IllegalArgumentException e) {
      // expected.
    }

    List<Integer> ints = new ArrayList<Integer>();
    for (int i = 0; i < 10; i++)
      ints.add(i);
    
    for (int i = 0; i < 10000; i++) {
      Integer j = randomFrom(ints);
      if (j != null) {
        ints.set(j, null);
      }
    }
    
    for (int i = 0; i < ints.size(); i++)
      assertTrue(ints.get(i) == null);
  }

  @Test
  public void testNewTempDir() throws IOException {
    for (int i = 0; i < 10; i++) {
      Path dir = newTempDir();
      assertNotNull(dir);
      assertTrue(Files.isDirectory(dir));
      assertTrue(Files.isWritable(dir));
      assertTrue(Files.isReadable(dir));
      assertTrue(Files.isExecutable(dir));
      try (DirectoryStream<Path> path = Files.newDirectoryStream(dir)) {
        assertFalse(path.iterator().hasNext());
      }
    }
  }

  @Test
  public void testNewTempFile() throws IOException {
    for (int i = 0; i < 10; i++) {
      Path file = newTempFile();
      assertNotNull(file);
      assertTrue(Files.isRegularFile(file));
      assertTrue(Files.isWritable(file));
      assertTrue(Files.isReadable(file));
      assertTrue(file.getFileName().toString().indexOf(' ') >= 0);
      Files.newOutputStream(file).close();
    }
  }

  @Test
  public void testRandomLocale() {
    assertNotNull(randomLocale());
  }

  @Test
  public void testRandomTimeZone() throws Exception {
    try {
      final String[] availableIDs = TimeZone.getAvailableIDs();
      Arrays.sort(availableIDs);
      for (String id : availableIDs) {
        assertNotNull(id);
        if (TimeZone.getTimeZone(id) == null) {
          fail("getTimeZone null: " + id);
        }
      }
    } catch (Exception e) {
      System.out.println("Wtf.");
      e.printStackTrace();
      throw e;
    }

    assertNotNull(randomTimeZone());
  }

  @Test
  public void testRandomAsciiOfLength() {
    assertTrue(randomAsciiLettersOfLength(0).isEmpty());

    for (int i = 0; i < 1000; i++) { 
      int maxLength = randomIntBetween(0, 20);
      String str = randomAsciiLettersOfLength(maxLength);
      assertTrue(str.matches("[a-zA-Z]*"));
      assertTrue(str.length() <= maxLength);
    }
  }

  @Test
  public void testRandomAlphanumOfLength() {
    assertTrue(randomAsciiAlphanumOfLength(0).isEmpty());

    for (int i = 0; i < 1000; i++) { 
      int maxLength = randomIntBetween(0, 20);
      String str = randomAsciiAlphanumOfLength(maxLength);
      assertTrue(str.matches("[a-zA-Z0-9]*"));
      assertTrue(str.length() <= maxLength);
    }
  }

  @Test
  public void testRandomUnicodeOfLength() {
    for (int i = 0; i < 1000; i++) { 
      int maxLength = randomIntBetween(0, 20);
      String str = randomUnicodeOfLength(maxLength);
      assertTrue(str.length() + " " + maxLength, str.length() <= maxLength);
    }
  }

  @Test
  public void testRandomRealisticUnicodeOfLength() {
    assertTrue(randomRealisticUnicodeOfLength(0).isEmpty());
    assertTrue(randomRealisticUnicodeOfCodepointLength(0).isEmpty());

    for (int i = 0; i < 1000; i++) { 
      int minLength = randomIntBetween(0, 20);
      int maxLength = minLength + randomIntBetween(0, 20);
      String str = randomRealisticUnicodeOfCodepointLength(maxLength);
      int codepoints = countCodepoints(str);
      assertTrue(codepoints <= maxLength);

      str = randomRealisticUnicodeOfCodepointLengthBetween(minLength, maxLength);
      codepoints = countCodepoints(str);
      assertTrue(codepoints >= minLength);
      assertTrue(codepoints <= maxLength);
    }
  }

  private static int countCodepoints(String str) {
    return str.codePointCount(0, str.length());
  }

  @Test
  public void testAssumeTrue() {
    String message = randomUnicodeOfLength(10);
    try {
      assumeTrue(message, false);
    } catch (AssumptionViolatedException e) {
      assertTrue(e.getMessage().contains(message));
    }
  }
  
  @Test
  public void testAssumeNoException() {
    String message = randomUnicodeOfLength(10);
    Throwable t = new Throwable();
    try {
      assumeNoException(message, t);
    } catch (AssumptionViolatedException e) {
      assertTrue(e.getMessage().contains(message));
      assertSame(t, e.getCause());
    }
  }

  @Test
  public void testIterations() {
    assertEquals(0, iterations(0, 0));
    assertEquals(Integer.MAX_VALUE, iterations(Integer.MAX_VALUE, Integer.MAX_VALUE));

    for (int i = 0; i < iterations(1, 1000); i++) {
      int j = iterations(0, 100);
      assertTrue(j >= 0 && j <= 100);
    }
  }

  @Test
  public void testNewServerSocket() throws IOException {
    ServerSocket socket = newServerSocket(LifecycleScope.TEST);
    socket.close();
  }

  @Test
  public void testRarely() throws IOException {
    int rarely = 0;
    int calls = 100000;
    for (int i = 0; i < calls; i++) {
      if (rarely()) rarely++;
    }

    double rf = rarely / (double) calls * 100;
    assertTrue("rarely should be > 5% & < 15%: " + rf, rf > 5 && rf < 15);
  }  
}
