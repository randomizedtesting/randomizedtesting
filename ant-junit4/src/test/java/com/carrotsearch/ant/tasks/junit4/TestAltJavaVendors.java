package com.carrotsearch.ant.tasks.junit4;


import java.io.File;
import java.net.URL;

import org.apache.tools.ant.types.Path;
import org.junit.*;


public class TestAltJavaVendors extends AntBuildFileTestBase {

  @Before
  public void setUp() throws Exception {
    URL resource = getClass().getClassLoader().getResource("junit4.xml");
    super.setupProject(new File(resource.getFile()));
  }

  @Test
  public void altVendors() {
    String altVendors = System.getProperty("alt.jvms");
    Assume.assumeTrue(altVendors != null && !altVendors.trim().isEmpty());

    for (String jvm : new Path(getProject(), altVendors).list()) {
      getProject().log("Trying JVM: " + jvm);
      Assert.assertTrue("JVM is not a file: " + jvm, new File(jvm).isFile());
      Assert.assertTrue("JVM is not executable: " + jvm, new File(jvm).canExecute());

      getProject().setProperty("jvm.exec", jvm);
      expectBuildExceptionContaining("alt-vendor", 
          "1 suite, 5 tests, 1 error, 1 failure, 2 ignored (1 assumption)");
    }
  }
}
