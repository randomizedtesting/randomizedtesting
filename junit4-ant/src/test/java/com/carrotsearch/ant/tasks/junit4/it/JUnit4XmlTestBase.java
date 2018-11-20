package com.carrotsearch.ant.tasks.junit4.it;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    URI resource = getClass().getClassLoader().getResource("junit4.xml").toURI();
    if (!resource.getScheme().equals("file")) {
      throw new IOException("junit4.xml not under a file URI: " + resource);
    }

    Path absolute = Paths.get(resource).toAbsolutePath();
    // System.out.println("junit4.xml at: " + absolute + (Files.exists(absolute) ? " (exists)" : " (does not exist)"));

    super.setupProject(absolute.toFile());
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
