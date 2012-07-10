package com.carrotsearch.randomizedtesting.rules;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

/**
 * An abstract {@link TestRule} that guarantees the execution of
 * {@link #afterAlways} even if an exception has been thrown from delegate
 * {@link Statement}. This is much like {@link AfterClass} or {@link After}
 * annotations but can be used with {@link RuleChain} to guarantee the order of
 * execution.
 */
public abstract class TestRuleAdapter implements TestRule {
  @Override
  public Statement apply(final Statement s, final Description d) {
    return new StatementAdapter(s) {
      @Override
      protected void before() throws Throwable {
        TestRuleAdapter.this.before();
      }
      
      @Override
      protected void afterAlways(List<Throwable> errors) throws Throwable {
        TestRuleAdapter.this.afterAlways(errors);
      }
      
      @Override
      protected void afterIfSuccessful() throws Throwable {
        TestRuleAdapter.this.afterIfSuccessful();
      }
    };
  }

  /**
   * Always called before the delegate {@link Statement}.
   */
  protected void before() throws Throwable {}
  
  /**
   * Always called after the delegate {@link Statement}, even if an exception
   * (or assumption failure) occurs. Any exceptions thrown from the body of this
   * method will be chained using {@link MultipleFailureException}.
   * 
   * @param errors
   *          A list of errors received so far. The list is modifiable although
   *          should only be extended with new potential exceptions.
   */
  protected void afterAlways(List<Throwable> errors) throws Throwable {}

  /**
   * Called only if the delegate {@link Statement} returned successfully.
   */
  protected void afterIfSuccessful() throws Throwable {}
}
