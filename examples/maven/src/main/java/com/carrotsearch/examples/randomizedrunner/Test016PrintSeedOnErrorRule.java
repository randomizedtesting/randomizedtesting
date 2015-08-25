package com.carrotsearch.examples.randomizedrunner;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.annotations.Seed;
import com.carrotsearch.randomizedtesting.rules.PrintSeedOnErrorRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A simple test which introduces the {@link PrintSeedOnErrorRule}. This JUnit-Rule allows the user to easily print the seed with which a test has
 * failed if and only if it fails. This allows for easier reproducability without clogging the output.
 *
 * @author blalasaadri
 */
@RunWith(RandomizedRunner.class)
@Seed("B3D0ED05E80E2276")
public class Test016PrintSeedOnErrorRule {

    @Rule
    public PrintSeedOnErrorRule printSeedOnErrorRule = new PrintSeedOnErrorRule();

    @Test
    public void failQuickly() {
        // Will print the seed
        Assert.fail();
    }

    @Test
    public void dontFail() {
        // Will not print the seed
        Assert.assertTrue(true);
    }

}
