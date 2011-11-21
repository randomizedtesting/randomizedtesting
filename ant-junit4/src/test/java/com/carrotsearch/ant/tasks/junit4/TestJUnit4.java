package com.carrotsearch.ant.tasks.junit4;

import java.net.URL;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.DefaultLogger;
import org.junit.Test;

public class TestJUnit4 extends BuildFileTest {
  private StringBuilder builder;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    URL resource = getClass().getClassLoader().getResource("junit4.xml");
    assertNotNull(resource);
    configureProject(resource.getFile());

    builder = new StringBuilder();
    getProject().addBuildListener(new DefaultLogger() {
      @Override
      public void messageLogged(BuildEvent e) {
        builder.append(e.getPriority() + " ");
        builder.append(e.getMessage());
        builder.append("\n");
      }
    });
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    // System.out.println(builder.toString());
    builder = null;
  }

  @Test
  public void testNormal() {
    super.executeTarget("normal");
  }

  @Test
  public void testNormalExecution() {
    super.executeTarget("dir");
  }
}
