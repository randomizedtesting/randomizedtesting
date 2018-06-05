package com.carrotsearch.ant.tasks.junit4.runlisteners;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class UserDefinedRunListener3 extends RunListener {

    @Override
    public void testRunStarted(Description description) throws Exception {
        System.out.print("UserDefinedRunListener3.testRunStarted()");
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        System.out.print("UserDefinedRunListener3.testRunFinished()");
    }

    @Override
    public void testStarted(Description description) throws Exception {
        System.out.print("UserDefinedRunListener3.testStarted()");
    }

    @Override
    public void testFinished(Description description) throws Exception {
        System.out.print("UserDefinedRunListener3.testFinished()");
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        System.out.print("UserDefinedRunListener3.testFailure()");
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        System.out.print("UserDefinedRunListener3.testAssumptionFailure()");
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        System.out.print("UserDefinedRunListener3.testIgnored()");
    }

}
