package com.carrotsearch.ant.tasks.junit4;


import java.io.File;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.carrotsearch.ant.tasks.junit4.tests.TestAfterClassError;
import com.carrotsearch.ant.tasks.junit4.tests.TestBeforeClassError;


public class TestJUnit4 extends AntBuildFileTestBase {

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

  @Test
  public void sysstreams() {
    executeTarget("sysstreams");
    assertLogContains("Tests summary: 1 suite, 2 tests");
    assertLogContains("1> sysout-sysout-contd.");
    assertLogContains("2> syserr-syserr-contd.");
  }

  @Test
  public void escaping() {
    executeTarget("escaping");
    assertLogContains("\"-Dsysprop.key2=abc def\"");
  }

  @Test 
  public void nojunit() {
    expectBuildExceptionContaining("nojunit", "Forked JVM's classpath must include a junit4 JAR");
  }

  @Test 
  public void customprefix() {
    executeForkedTarget("customprefix");
  }

  @Test 
  public void oldjunit() {
    executeForkedTarget("oldjunit");
    assertLogContains("Forked JVM's classpath must use JUnit 4.10 or newer");
  }

  @Test 
  public void nojunit_task() {
    executeForkedTarget("nojunit-task");
    assertLogContains("JUnit JAR must be added to junit4 taskdef's classpath");
  }

  @Test 
  public void oldjunit_task() {
    executeForkedTarget("oldjunit-task");
    assertLogContains("At least JUnit version 4.10 is required on junit4's taskdef classpath");
  }

  @Test
  public void statuses() throws Throwable {
    expectBuildExceptionContaining("statuses", 
        "1 suite, 5 tests, 1 error, 1 failure, 2 ignored (1 assumption)");
  }

  @Test
  public void ignoredSuite() throws Throwable {
    executeTarget("ignoredSuite");
    assertLogContains("Tests summary: 1 suite, 0 tests");
  }

  @Test
  public void beforeClassError() throws Throwable {
    expectBuildExceptionContaining("beforeClassError", 
        "1 suite, 0 tests, 1 suite-level error");
    assertLogContains("| " + TestBeforeClassError.class.getSimpleName() + " (suite)");
  }

  @Test
  public void afterClassError() throws Throwable {
    expectBuildExceptionContaining("afterClassError", 
        "1 suite, 1 test, 1 suite-level error");
    assertLogContains("| " + TestAfterClassError.class.getSimpleName() + " (suite)");
  }

  @Test
  public void hierarchicalSuiteDescription() throws Throwable {
    expectBuildExceptionContaining("hierarchicalSuiteDescription", 
        "1 suite, 2 tests, 3 suite-level errors, 1 error");
  }

  @Test 
  public void dir() {
    executeTarget("dir");
  }
  
  @Test 
  public void maxmem() {
    executeTarget("maxmem");
  }  

  @Test 
  public void jvmarg() {
    executeTarget("jvmarg");
  }
  
  @Test 
  public void sysproperty() {
    executeTarget("sysproperty");
  }

  @Test 
  public void env() {
    executeTarget("env");
  }

  @Test 
  public void failureProperty() {
    executeTarget("failureProperty");
  }

  @Test
  public void failureTypePassing() {
    executeTarget("failureTypePassing");
    assertLogContains("Throwable #1: com.carrotsearch.ant.tasks.junit4.tests.SyntheticException");
    assertLogContains("Tests summary: 1 suite, 1 test, 1 error");
  }

  @Test
  public void jvmcrash() {
    expectBuildExceptionContaining("jvmcrash", "Unexpected output from forked JVM.");
    File cwd = getProject().getBaseDir();
    for (File crashDump : cwd.listFiles()) {
      if (crashDump.isFile() && 
          (crashDump.getName().matches("^hs_err_pid.+\\.log") ||
           crashDump.getName().endsWith(".mdmp") ||
           crashDump.getName().endsWith(".dmp") ||
           crashDump.getName().endsWith(".dump") ||
           crashDump.getName().endsWith(".trc"))) {
        crashDump.delete();
      }
    }
  }

  @Test
  public void seedpassing() {
    executeTarget("seedpassing");
  }

  @Test
  public void seedpassingInvalid() {
    expectBuildExceptionContaining("seedpassing.invalid", "Not a valid seed chain");
  }
  
  @Test
  public void reproducestring() {
    executeTarget("reproducestring");
    assertLogContains("2> Reproduce: ");
  }
  
  @Test
  public void assertions() {
    expectBuildExceptionContaining("assertions", "There were test failures");
    assertLogContains("> Throwable #1: java.lang.AssertionError: foobar");
  }

  @Test
  public void balancing() {
    executeTarget("balancing");
    assertLogContains("Assignment hint: J0  (cost  2019) com.carrotsearch.ant.tasks.junit4.tests.sub2.TestTwoSeconds (by ExecutionTimeBalancer)");
    assertLogContains("Assignment hint: J1  (cost  1002) com.carrotsearch.ant.tasks.junit4.tests.sub2.TestOneSecond (by ExecutionTimeBalancer)");
    assertLogContains("Assignment hint: J1  (cost   501) com.carrotsearch.ant.tasks.junit4.tests.sub2.TestHalfSecond (by ExecutionTimeBalancer)");
    assertLogContains("Assignment hint: J1  (cost     2) com.carrotsearch.ant.tasks.junit4.tests.sub2.TestZeroSeconds (by ExecutionTimeBalancer)");
  }

  @Test
  public void slavehanging() {
    executeTarget("slavehanging");
    assertLogContains("Caused by: java.lang.ArithmeticException");
  }

  @Test
  public void outofordersysouts() {
    executeTarget("outofordersysouts");
  }
  
  @Test
  public void mergehints() {
    executeTarget("mergehints");
  }

  @Test
  public void staticScopeOutput() {
    executeTarget("staticScopeOutput");
    assertLogContains("1> static-scope");
    assertLogContains("1> before-class");
    assertLogContains("1> after-class");
  }
  
  @Test
  public void iters() {
    executeTarget("iters");
    Pattern p = Pattern.compile("TestSuccess\\.alwaysPasses");
    Matcher matcher = p.matcher(getLog());
    int count = 0;
    while (matcher.find()) {
      count++;
    }
    Assert.assertEquals(5, count);
  }  
}
