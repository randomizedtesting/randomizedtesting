package com.carrotsearch.randomizedtesting.generators;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

@Repeat(iterations = 100)
public class TestRandomBytes extends RandomizedTest {
  @Test
  public void testRandomBytes() {
    int len = randomIntBetween(0, 100);
    Assertions.assertThat(randomBytesOfLength(len)).hasSize(len);
  }

  @Test
  public void testRandomBytesOfLength() {
    int min = randomIntBetween(0, 100);
    int max = min + randomIntBetween(0, 10);

    byte[] bytes = randomBytesOfLength(min, max);
    Assertions.assertThat(bytes.length >= min).isTrue();
    Assertions.assertThat(bytes.length <= max).isTrue();
  }
}
