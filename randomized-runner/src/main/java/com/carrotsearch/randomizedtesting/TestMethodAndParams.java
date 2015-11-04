package com.carrotsearch.randomizedtesting;

import java.lang.reflect.Method;
import java.util.List;

import com.carrotsearch.randomizedtesting.annotations.TestCaseOrdering;

/**
 * A single test case entry composed of the test method
 * and the arguments eventually passed to the test class's constructor.
 * 
 * @see TestCaseOrdering
 */
public interface TestMethodAndParams {
  Method getTestMethod();
  List<Object> getInstanceArguments();
}
