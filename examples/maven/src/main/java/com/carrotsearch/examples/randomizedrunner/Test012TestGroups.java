package com.carrotsearch.examples.randomizedrunner;

import java.lang.annotation.*;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Nightly;
import com.carrotsearch.randomizedtesting.annotations.TestGroup;

/**
 * We introduced the notion of {@link Nightly} tests in
 * {@link Test011NightlyTests}. Similar to this idea, the user can introduce any
 * number of custom test groups which can be arbitrarily enabled or disabled. In
 * fact, {@link Nightly} is also defined as such a {@link TestGroup}.
 * 
 * <p>
 * A custom test group is an annotation itself annotated with a
 * {@link TestGroup}. For example, let's say we want a test group that marks all
 * tests that require a physical display. An annotation for this is shown in
 * {@link Test012TestGroups.RequiresDisplay}. It has no additional attributes.
 * What makes it a test group is a meta-annotation:
 * 
 * <pre>
 * {@literal @}{@link TestGroup}(name = "requiresdisplay", enabled = false, sysProperty = "display")
 * </pre>
 * 
 * which states that the group's name is "requiresdisplay" and that the group is
 * initially disabled unless a system property "display" is set to a boolean
 * value "false", "off" or "disabled".
 * 
 * <p>
 * {@link Nightly} is defined in a very similar way. Note that test groups are
 * real annotations so they are recognizable by IDEs, can be searched,
 * manipulated etc.
 * 
 * <p>
 * Another feature of using {@link RandomizedRunner} with groups is the ability to specify
 * complex group-based filters specified via <code>tests.filter</code> system property.
 * These filters are boolean conditions, with optional parentheses. For example:
 * <ul>
 *  <li><code>{@literal @}nightly</code> - runs all tests with test group named <i>nightly</i>.</li>
 *  <li><code>not {@literal @}nightly</code> - runs all tests not annotated with a test group named <i>nightly</i>.</li>
 *  <li><code>{@literal @}fast and not {@literal @}requiresdisplay</code> - runs all tests annotated with <i>fast</i> and not annotated with <i>requiresdisplay</i>.</li>
 *  <li><code>not ({@literal @}slow or {@literal @}nightly)</code> - skips any tests annotated with <i>slow</i> or <i>nightly</i>.</li>  
 * </ul>
 * 
 * <strong>Important!</strong> Note that using filtering expression has precedence over the default state of a group
 * and its corresponding system property. This is intentional so that filtering expressions can be used
 * independently of each group's default state. Should the default state be taken into account one
 * can use a special keyword <code>default</code>, as in:
 * <ul>
 *  <li><code>default and not {@literal @}slow</code> - runs any tests that would be selected by their default
 *  group status (including those that do not have any test groups at all), 
 *  excluding any tests annotated with <i>slow</i>.</li> 
 * </ul>   
 */
public class Test012TestGroups extends RandomizedTest {
  @TestGroup(name = "requiresdisplay", enabled = false, sysProperty = "display")
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  public static @interface RequiresDisplay {}
  
  @Test
  @RequiresDisplay
  public void usesDisplay() {
    System.out.println("Running on windows.");
  }
}
