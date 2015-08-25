package com.carrotsearch.randomizedtesting.rules;

import com.carrotsearch.randomizedtesting.RandomizedContext;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Simple rule which prints the test seed only if a test fails.
 *
 * @author blalasaadri
 */
public class PrintSeedOnErrorRule implements TestRule {

    @Override
    public Statement apply(final Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    statement.evaluate();
                } catch(AssertionError e) {
                    System.err.println("Test failed with seed: " + RandomizedContext.current().getRunnerSeedAsString());
                    throw e;
                }
            }
        };
    }
}
