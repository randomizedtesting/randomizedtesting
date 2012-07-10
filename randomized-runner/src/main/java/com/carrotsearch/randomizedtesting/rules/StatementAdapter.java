package com.carrotsearch.randomizedtesting.rules;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.rules.RuleChain;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

/**
 * An abstract {@link Statement} that guarantees the execution of
 * {@link #afterAlways} even if an exception has been thrown from delegate
 * {@link Statement}. This is much like {@link AfterClass} or {@link After}
 * annotations but can be used with {@link RuleChain} to guarantee the order of
 * execution.
 */
public abstract class StatementAdapter extends Statement {
  
  private final Statement delegate;

  protected StatementAdapter(Statement delegate) {
    this.delegate = delegate;
  }
  
  /**
   * 
   */
  @Override
  final public void evaluate() throws Throwable {
    final ArrayList<Throwable> errors = new ArrayList<Throwable>();

    try {
      before();
      delegate.evaluate();
      afterIfSuccessful();
    } catch (Throwable t) {
      errors.add(t);
    }
    
    try {
      afterAlways(errors);
    } catch (Throwable t) {
      errors.add(t);
    }
    
    MultipleFailureException.assertEmpty(errors);
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
