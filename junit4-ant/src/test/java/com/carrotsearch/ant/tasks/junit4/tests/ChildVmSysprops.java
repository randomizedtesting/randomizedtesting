package com.carrotsearch.ant.tasks.junit4.tests;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class ChildVmSysprops {
  @Test
  public void cwd() {
    Assert.assertNotNull(System.getProperty("junit4.childvm.cwd"));
    Assert.assertTrue(new File(System.getProperty("junit4.childvm.cwd")).isDirectory());
  }
  
  @Test
  public void id() {
    Assert.assertNotNull(Integer.parseInt(System.getProperty("junit4.childvm.id")));
  }  
}
