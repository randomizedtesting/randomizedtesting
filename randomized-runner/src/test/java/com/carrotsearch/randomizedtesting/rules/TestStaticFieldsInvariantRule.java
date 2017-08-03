package com.carrotsearch.randomizedtesting.rules;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;

public class TestStaticFieldsInvariantRule extends WithNestedTestClass {
  static int LEAK_THRESHOLD = 5 * 1024 * 1024;

  @ThreadLeakScope(Scope.SUITE)
  public static class Base extends RandomizedTest {
    private static TestRule assumeNotNestedRule = new TestRule() {
      public Statement apply(final Statement base, Description description) {
        return new Statement() {
          public void evaluate() throws Throwable {
            assumeRunningNested();
            base.evaluate();
          }
        };
      }
    };
    
    @ClassRule
    public static TestRule classRules = 
      RuleChain
        .outerRule(assumeNotNestedRule)
        .around(new StaticFieldsInvariantRule(LEAK_THRESHOLD, true));

    @Test
    public void testEmpty() {}
  }

  public static class Smaller extends Base {
    static byte [] field0; 
    
    @BeforeClass
    private static void setup() {
      field0 = new byte [LEAK_THRESHOLD / 2];
    }
  }

  public static class Exceeding extends Smaller {
    static byte [] field1;
    static byte [] field2;
    static int [] field3;
    static long field4;
    final static long [] field5 = new long [1024];

    @BeforeClass
    private static void setup() {
      field1 = new byte [LEAK_THRESHOLD / 2];
      field2 = new byte [100];
      field3 = new int [100];
    }
  }

  public static class MultipleReferences extends Base {
    static Object ref1, ref2, ref3,
                  ref4, ref5, ref6;

    static Object ref7 = null;
    
    static {
      Map<String,Object> map = new HashMap<String,Object>();
      map.put("key", new byte [1024 * 1024 * 2]);
      ref1 = ref2 = ref3 = ref4 = ref5 = ref6 = map;
    }
  }

  @Test
  public void testReferencesCountedMultipleTimes() { 
    FullResult runClasses = runTests(MultipleReferences.class);
    Assertions.assertThat(runClasses.getFailures()).isEmpty();
  }

  @Test
  public void testPassingUnderThreshold() {
    FullResult runClasses = runTests(Smaller.class);
    Assertions.assertThat(runClasses.getFailures()).isEmpty();
  }
  
  @Test
  public void testFailingAboveThreshold() {
    FullResult runClasses = runTests(Exceeding.class);
    Assertions.assertThat(runClasses.getFailures()).hasSize(1);
    
    Assertions.assertThat(runClasses.getFailures().get(0).getTrace())
      .contains(".field0")
      .contains(".field1")
      .contains(".field2")
      .contains(".field3")
      .doesNotContain(".field5");
  }
  
  static class Holder {
    private final Path path;
    
    Holder() {
      this.path = Paths.get(".");
      final String name = this.path.getClass().getName();
      RandomizedTest.assumeTrue(Path.class.getName() + " is not implemented by internal class in this JVM: " + name,
        name.startsWith("sun.") || name.startsWith("jdk."));
    }
  }
  
  public static class FailsJava9 extends Base {
    static Holder field0; 
    
    @BeforeClass
    private static void setup() throws Exception {
      field0 = new Holder();
    }
  }

  @Test @org.junit.Ignore
  public void testJava9Jigsaw() {
    // check if we have Java 9 module system:
    try {
      Class.class.getMethod("getModule");
    } catch (Exception e) {
      RandomizedTest.assumeTrue("This test requires Java 9 module system (Jigsaw)", false);
    }
  
    FullResult runClasses = runTests(FailsJava9.class);
    Assertions.assertThat(runClasses.getFailures()).hasSize(1);
    
    Assertions.assertThat(runClasses.getFailures().get(0).getTrace())
      .contains("sizes cannot be measured due to security restrictions or Java 9")
      .contains(".field0");
  }
}
