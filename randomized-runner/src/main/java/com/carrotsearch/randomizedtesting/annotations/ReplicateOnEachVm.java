package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Replicates the test class on each concurrent forked JVM. If only a single JVM
 * is used for running tests, this annotation has no effect.</p>
 * 
 * <p>The purpose of this annotation is to, for example, replicate a single test suite
 * across multiple forked JVMs and then selectively ignore or execute tests on each
 * JVM, depending on its number, providing poor-man's load balancing for individual 
 * test cases (test suites are balanced by the framework itself).</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface ReplicateOnEachVm {}