package com.carrotsearch.randomizedtesting;

import org.junit.Test;

/**
 * Method provider selecting {@link Test} annotated public instance parameterless methods.
 */
public class JUnit4MethodProvider extends AnnotatedMethodProvider {
  public JUnit4MethodProvider() {
    super(Test.class);
  }
}
