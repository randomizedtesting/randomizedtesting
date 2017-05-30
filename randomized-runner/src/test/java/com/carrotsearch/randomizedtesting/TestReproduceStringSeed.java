package com.carrotsearch.randomizedtesting;

import com.carrotsearch.randomizedtesting.annotations.Seed;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;

import static org.junit.internal.matchers.StringContains.containsString;

@RunWith(com.carrotsearch.randomizedtesting.RandomizedRunner.class)
@Seed("deadbeef")
public class TestReproduceStringSeed extends RandomizedTest {
  @Seed("cafebabe")
  @Test
  public void testErrorStringIsBuiltCorrectly() {
    final StringBuilder b = new StringBuilder();
    Description description = Description.createTestDescription(TestReproduceStringSeed.class, "testErrorStringIsBuiltCorrectly");
    new ReproduceErrorMessageBuilder(b).appendAllOpts(description);
    assertThat(b.toString(), containsString("DEADBEEF:CAFEBABE"));
    assertFalse(b.toString().contains("[DEADBEEF:CAFEBABE]"));
  }
}
