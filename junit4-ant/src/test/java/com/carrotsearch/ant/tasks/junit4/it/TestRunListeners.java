package com.carrotsearch.ant.tasks.junit4.it;

import org.junit.Test;

public class TestRunListeners extends JUnit4XmlTestBase {

    @Test
    public void singleUserDefinedRunListener() {
        super.executeTarget("singleUserDefinedRunListener");

        assertLogContains("UserDefinedRunListener1.testStarted()");
        assertLogContains("UserDefinedRunListener1.testFinished()");
    }

    @Test
    public void multipleUserDefinedRunListeners() {
        super.executeTarget("multipleUserDefinedRunListeners");

        assertLogContains("UserDefinedRunListener2.testStarted()");
        assertLogContains("UserDefinedRunListener2.testFinished()");

        assertLogContains("UserDefinedRunListener3.testStarted()");
        assertLogContains("UserDefinedRunListener3.testFinished()");
    }

}
