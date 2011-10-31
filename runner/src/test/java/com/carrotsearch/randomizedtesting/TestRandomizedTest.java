package com.carrotsearch.randomizedtesting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;

public class TestRandomizedTest extends RandomizedTest {
  @Test
  public void testRandomInt() {
    boolean [] array = new boolean [10];
    for (int i = 0; i < 10000; i++) 
      array[randomInt(array.length - 1)] = true;

    for (boolean b: array) 
      assertTrue(b);
  }

  @Test
  public void testRandomIntBetween() {
    boolean [] array = new boolean [10];
    for (int i = 0; i < 10000; i++) 
      array[randomIntBetween(0, array.length - 1)] = true;

    for (boolean b: array) 
      assertTrue(b);
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
  public void testNewTempDir() {
    for (int i = 0; i < 10; i++) {
      File dir = newTempDir();
      assertNotNull(dir);
      assertTrue(dir.isDirectory());
      assertTrue(dir.canWrite());
      assertTrue(dir.canRead());
      assertTrue(dir.canExecute());
      assertEquals(0, dir.listFiles().length);
    }
  }

  @Test
  public void testNewTempFile() throws IOException {
    for (int i = 0; i < 10; i++) {
      File file = newTempFile();
      assertNotNull(file);
      assertTrue(file.isFile());
      assertTrue(file.canWrite());
      assertTrue(file.canRead());

      new FileOutputStream(file).close();
    }
  }

  @Test
  public void testRandomLocale() {
    assertNotNull(randomLocale());
  }

  @Test
  public void testRandomTimeZone() {
    assertNotNull(randomTimeZone());
  }

  @Test
  public void testRandomCharString() {
    randomCharString('a', 'z', 0);
    
    for (int i = 0; i < 1000; i++) { 
      int maxLength = randomInt(20);
      String str = randomCharString('a', 'z', maxLength);
      assertTrue(str.matches("[a-z]*"));
      assertTrue(str.length() <= maxLength);
    }
  }

  @Test
  public void testRandomUnicodeString() {
    for (int i = 0; i < 1000; i++) { 
      int maxLength = randomInt(20);
      String str = randomUnicodeString(maxLength);
      assertTrue(str.length() + " " + maxLength, str.length() <= maxLength);
    }
  }

  @Test
  public void testRandomUnicodeStringOfUtf16Length() {
    for (int i = 0; i < 1000; i++) { 
      int maxLength = randomInt(20);
      String str = randomUnicodeStringOfUTF16Length(maxLength);
      assertEquals(maxLength, str.length());
    }
  }

  @Test
  public void testRandomUnicodeStringOfUTF8Length() {
    for (int i = 0; i < 1000; i++) { 
      int maxLength = randomInt(20);
      String str = randomUnicodeStringOfUTF8Length(maxLength);
      byte[] utf8 = str.getBytes(UTF8);
      assertTrue(utf8.length <= maxLength);
    }
  }

  @Test
  public void testRandomRealisticUnicodeString() {
    assertTrue(randomRealisticUnicodeString(0).isEmpty());

    for (int i = 0; i < 1000; i++) { 
      int minLength = randomInt(20);
      int maxLength = minLength + randomInt(20);
      String str = randomRealisticUnicodeString(maxLength);
      int codepoints = countCodepoints(str);
      assertTrue(codepoints <= maxLength);

      str = randomRealisticUnicodeString(minLength, maxLength);
      codepoints = countCodepoints(str);
      assertTrue(codepoints >= minLength);
      assertTrue(codepoints <= maxLength);
    }
  }

  private static int countCodepoints(String str) {
    return str.getBytes(UTF32).length / 4;
  }
  
  @Test
  public void testAssumeTrue() {
    String message = randomUnicodeStringOfUTF16Length(10);
    try {
      assumeTrue(message, false);
    } catch (AssumptionViolatedException e) {
      assertTrue(e.getMessage().contains(message));
    }
  }
  
  @Test
  public void testAssumeNoException() {
    String message = randomUnicodeStringOfUTF16Length(10);
    Throwable t = new Throwable();
    try {
      assumeNoException(message, t);
    } catch (AssumptionViolatedException e) {
      assertTrue(e.getMessage().contains(message));
      assertSame(t, e.getCause());
    }
  }  
}
