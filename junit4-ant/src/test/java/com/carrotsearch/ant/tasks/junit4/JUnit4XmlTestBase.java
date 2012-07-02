package com.carrotsearch.ant.tasks.junit4;

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A base class for tests contained in <code>junit4.xml</code>
 * file.
 */
public class JUnit4XmlTestBase extends AntBuildFileTestBase {
  @Rule
  public TestRule dumpLogOnError = new TestRule() {
    @Override
    public Statement apply(final Statement base, Description description) {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          try {
            base.evaluate();
          } catch (Throwable e) {
            System.out.println("Ant log: " + getLog());
            throw e;
          }
        }
      };
    }
  };
  
  @Before
  public void setUp() throws Exception {
    URL resource = getClass().getClassLoader().getResource("junit4.xml");
    super.setupProject(new File(resource.getFile()));
  }
  
  protected static int countPattern(String output, String substr) {
    int count = 0;
    for (int i = 0; i < output.length();) {
      int index = output.indexOf(substr, i);
      if (index < 0) {
        break;
      }
      count++;
      i = index + 1;
    }
    return count;
  }  
}
