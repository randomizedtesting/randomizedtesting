package com.carrotsearch.randomizedtesting.rules;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.*;
import org.junit.runner.notification.Failure;
import org.junit.runners.model.Statement;

import com.carrotsearch.randomizedtesting.WithNestedTestClass;

public class TestSystemPropertiesInvariantRule extends WithNestedTestClass {
  public static final String PROP_KEY1 = "new-property-1";
  public static final String VALUE1 = "new-value-1";
  
  public static class Base {
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
      RuleChain.outerRule(assumeNotNestedRule).around(new SystemPropertiesInvariantRule());

    @Rule
    public TestRule testRules = 
      RuleChain.outerRule(new SystemPropertiesInvariantRule());

    @Test
    public void testEmpty() {}
  }
  
  public static class InBeforeClass extends Base {
    @BeforeClass
    public static void beforeClass() {
      System.setProperty(PROP_KEY1, VALUE1);
    }
  }
  
  public static class InAfterClass extends Base {
    @AfterClass
    public static void afterClass() {
      System.setProperty(PROP_KEY1, VALUE1);
    }
  }
  
  public static class InTestMethod extends Base {
    @Test
    public void testMethod1() {
      if (System.getProperty(PROP_KEY1) != null) {
        throw new RuntimeException("Shouldn't be here.");
      }
      System.setProperty(PROP_KEY1, VALUE1);
    }
    
    @Test
    public void testMethod2() {
      testMethod1();
    }
  }

  public static class NonStringProperties extends Base {
    @Test
    public void testMethod1() {
      if (System.getProperties().get(PROP_KEY1) != null) {
        throw new RuntimeException("Will pass.");
      }

      Properties properties = System.getProperties();
      properties.put(PROP_KEY1, new Object());
      Assert.assertTrue(System.getProperties().get(PROP_KEY1) != null);
    }

    @Test
    public void testMethod2() {
      testMethod1();
    }

    @AfterClass
    public static void cleanup() {
      System.getProperties().remove(PROP_KEY1);
    }
  }

  @Test
  public void testRuleInvariantBeforeClass() {
    FullResult runClasses = runTests(InBeforeClass.class);
    Assert.assertEquals(1, runClasses.getFailureCount());
    Assert.assertTrue(runClasses.getFailures().get(0).getMessage()
        .contains(PROP_KEY1));
    Assert.assertNull(System.getProperty(PROP_KEY1));
  }
  
  @Test
  public void testRuleInvariantAfterClass() {
    FullResult runClasses = runTests(InAfterClass.class);
    Assert.assertEquals(1, runClasses.getFailureCount());
    Assert.assertTrue(runClasses.getFailures().get(0).getMessage()
        .contains(PROP_KEY1));
    Assert.assertNull(System.getProperty(PROP_KEY1));
  }
  
  @Test
  public void testRuleInvariantInTestMethod() {
    FullResult runClasses = runTests(InTestMethod.class);
    Assert.assertEquals(2, runClasses.getFailureCount());
    for (Failure f : runClasses.getFailures()) {
      Assert.assertTrue(f.getMessage().contains(PROP_KEY1));
    }
    Assert.assertNull(System.getProperty(PROP_KEY1));
  }
  
  @Test
  public void testNonStringProperties() {
    FullResult runClasses = runTests(NonStringProperties.class);
    Assert.assertEquals(1, runClasses.getFailureCount());
    Assert.assertTrue(runClasses.getFailures().get(0).getMessage().contains("Will pass"));
    Assert.assertEquals(3, runClasses.getRunCount());
  }
}
