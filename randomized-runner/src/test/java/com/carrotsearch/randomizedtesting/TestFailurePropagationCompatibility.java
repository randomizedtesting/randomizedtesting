package com.carrotsearch.randomizedtesting;

import java.util.ArrayList;
import java.util.Random;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.*;
import org.junit.runner.notification.Failure;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;


/**
 * Check failure propagation compatibility against JUnit.
 */
public class TestFailurePropagationCompatibility extends WithNestedTestClass {
  static Random random;
  static int frequency;

  public static class MaybeFailRule implements TestRule {
    @Override
    public Statement apply(final Statement base, Description description) {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          try {
            maybeFail();
            base.evaluate();
            maybeFail();
          } catch (Throwable t) {
            maybeFail();
          } finally {
            maybeFail();
          }
        }
      };
    }
  }
  
  public static class FailRandomly1 {
    public FailRandomly1() {
      maybeFail();
    }

    @ClassRule
    public static TestRule classRule1 = RuleChain
        .outerRule(new MaybeFailRule())
        .around(new MaybeFailRule());

    @Rule
    public TestRule testRule1 = RuleChain
        .outerRule(new MaybeFailRule())
        .around(new MaybeFailRule());

    @BeforeClass public static void beforeClass1() { maybeFail(); }
    @Before      public        void before()       { maybeFail(); }
    @Test        public        void test()         { maybeFail(); }
    @After       public        void after()        { maybeFail(); }
    @AfterClass  public static void afterClass()   { maybeFail(); }
  }

  public static class FailRandomly2 extends FailRandomly1 {
    public FailRandomly2() {
      maybeFail();
    }

    @ClassRule
    public static TestRule classRule2 = RuleChain
        .outerRule(new MaybeFailRule())
        .around(new MaybeFailRule());

    @Rule
    public TestRule testRule2 = RuleChain
        .outerRule(new MaybeFailRule())
        .around(new MaybeFailRule());

    @BeforeClass public static void beforeClass2()  { maybeFail(); }
    @Before      public        void before2()       { maybeFail(); }
    @After       public        void after2()        { maybeFail(); }
    @AfterClass  public static void afterClass2()   { maybeFail(); }
  }
  
  @RunWith(RandomizedRunner.class)
  public static class FailRandomly3 extends FailRandomly2 {
  }

  static void maybeFail() {
    if (random != null) {
      if (random.nextInt(frequency) == 0) {
        if (random.nextBoolean()) {
          throw new RuntimeException("State: " + random.nextLong());
        } else {
          ArrayList<Throwable> errors = new ArrayList<Throwable>();
          errors.add(new RuntimeException("State: " + random.nextLong()));
          errors.add(new RuntimeException("State: " + random.nextLong()));
          // Throw MultipleFailureException as if unchecked.
          Rethrow.rethrow(new MultipleFailureException(errors));
        }
      }
    }
  }
  
  @Test
  public void testRunEquals() throws Exception {
    Random rnd = new Random(); // initial.
    for (int f = 1; f < 10; f += 2) {
      frequency = f;
      for (int i = 0; i < 25; i++) {
        final long seed = rnd.nextLong() + i;
        random = new Random(seed);
        Result junit4 = new JUnitCore().run(FailRandomly2.class);
        random = new Random(seed);
        Result rr = new JUnitCore().run(FailRandomly3.class);
  
        String traceJunit4 = executionTrace(junit4);
        String traceRR = executionTrace(rr);
        if (!traceJunit4.equals(traceRR)) {
          System.out.println("=== Random(" + seed + "), freq: " + frequency);
          System.out.println("--- JUnit4:");
          System.out.println(traceJunit4);
          System.out.println("--- RandomizedRunner:");
          System.out.println(traceRR);
          Assert.fail();
        }
      }
    }
  }
  
  @AfterClass
  public static void cleanup() {
    random = null;
  }

  private String executionTrace(Result r) {
    StringBuilder sb = new StringBuilder();
    sb.append("Run count: " + r.getRunCount() + "\n");
    sb.append("Ignore count: " + r.getIgnoreCount() + "\n");
    sb.append("Failure count: " + r.getFailureCount() + "\n");
    for (Failure f : r.getFailures()) {
      Throwable t = f.getException();
      sb.append(t.getClass().getName() + ": " + t.getMessage());
      sb.append("\n\n");
    }
    return sb.toString();
  }
}
