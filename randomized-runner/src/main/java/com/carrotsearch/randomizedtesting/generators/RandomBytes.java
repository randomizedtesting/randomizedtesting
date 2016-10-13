package com.carrotsearch.randomizedtesting.generators;

import java.util.Random;

/**
 * Random byte sequence generators.
 */
public final class RandomBytes {
  /**
   * @param r Random generator.
   * @param length The length of the byte array. Can be zero.
   * @return Returns a byte array with random content.
   */
  public static byte[] randomBytesOfLength(Random r, int length) {
    return randomBytesOfLengthBetween(r, length, length);
  }
  
  /**
   * @param r Random generator.
   * @param minLength The minimum length of the byte array. Can be zero.
   * @param maxLength The maximum length of the byte array. Can be zero.
   * @return Returns a byte array with random content.
   */
  public static byte[] randomBytesOfLengthBetween(Random r, int minLength, int maxLength) {
    byte[] bytes = new byte[RandomNumbers.randomIntBetween(r, minLength, maxLength)];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = (byte) r.nextInt();
    }
    return bytes;
  }
}
