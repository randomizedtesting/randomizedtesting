package com.carrotsearch.randomizedtesting;

import java.util.Random;

/**
 * Implements Xoroshiro128PlusRandom. Not synchronized (anywhere).
 * 
 * @see "http://xoroshiro.di.unimi.it/"
 */
@SuppressWarnings("serial")
public class Xoroshiro128PlusRandom extends Random {
  private long s0, s1;

  public Xoroshiro128PlusRandom(long seed) {
    // Must be here, the only Random constructor. Has side-effects on setSeed, see below.
    super(0);

    s0 = MurmurHash3.hash(seed);
    s1 = MurmurHash3.hash(s0);

    if (s0 == 0 && s1 == 0) {
      s0 = MurmurHash3.hash(0xdeadbeefL);
      s1 = MurmurHash3.hash(s0);
    }
  }

  @Override
  public void setSeed(long seed) {
    // Called from super constructor and observing uninitialized state?
    if (s0 == 0 && s1 == 0) {
      return;
    }

    throw AssertingRandom.noSetSeet();
  }
  
  @Override
  public boolean nextBoolean() {
    return nextLong() >= 0;
  }

  @Override
  public void nextBytes(byte[] bytes) {
    for (int i = 0, len = bytes.length; i < len; ) {
      long rnd = nextInt();
      for (int n = Math.min(len - i, 8); n-- > 0; rnd >>>= 8) {
        bytes[i++] = (byte) rnd;
      }
    }
  }

  @Override
  public double nextDouble() {
    return Double.longBitsToDouble((nextLong() >>> 12) | (0x3FFL << 52)) - 1.0d; 
  }

  @Override
  public float nextFloat() {
    return Float.intBitsToFloat(((int)(nextLong() >>> 41)) | (0x3F8 << 20)) - 1.0f; 
  }
  
  @Override
  public int nextInt() {
    return (int) nextLong();
  }
  
  @Override
  public int nextInt(int n) {
    // Leave superclass's implementation.
    return super.nextInt(n);
  }

  @Override
  public double nextGaussian() {
    // Leave superclass's implementation.
    return super.nextGaussian();
  }

  @Override
  public long nextLong() {
    final long s0 = this.s0;
    long s1 = this.s1;
    final long result = s0 + s1;
    s1 ^= s0;
    this.s0 = Long.rotateLeft(s0, 55) ^ s1 ^ s1 << 14;
    this.s1 = Long.rotateLeft(s1, 36);
    return result; 
  }
  
  @Override
  protected int next(int bits) {
    return ((int) nextLong()) >>> (32 - bits);
  }  
}
