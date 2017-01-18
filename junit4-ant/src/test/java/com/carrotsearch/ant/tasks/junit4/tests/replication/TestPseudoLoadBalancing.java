package com.carrotsearch.ant.tasks.junit4.tests.replication;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Name;
import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import com.carrotsearch.randomizedtesting.annotations.ReplicateOnEachVm;

@ReplicateOnEachVm
public class TestPseudoLoadBalancing extends RandomizedTest {
  private int id;
  private int jvmId;
  private int jvms;

  public TestPseudoLoadBalancing(@Name("id")  int id) {
    this.id = id;
  }
  
  @Before
  public void pseudoLoadBalancing() {
    jvmId = Integer.parseInt(System.getProperty("junit4.childvm.id"));
    jvms = Integer.parseInt(System.getProperty("junit4.childvm.count"));

    assumeTrue(
        String.format(Locale.ROOT, "Test %d ignored on VM %d.", id, jvmId),
        (id % jvms) == jvmId);
  }

  @Test
  public void replicatedTest() {
    System.out.println(String.format(Locale.ROOT, "Test %d executed on VM %d.", id, jvmId));
  }

  @ParametersFactory
  public static Iterable<Object[]> parameters() {
    List<Object[]> args = new ArrayList<Object[]>();
    for (int i = 0; i < 100; i++) {
      args.add($(i));
    }
    return args;
  }
}
