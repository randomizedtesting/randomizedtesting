package com.carrotsearch.examples.randomizedrunner;

import java.util.Arrays;
import java.util.Formatter;

import org.junit.Test;
import org.junit.runners.Parameterized;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Name;
import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Seed;
import com.carrotsearch.randomizedtesting.annotations.Seeds;

/**
 * Parameterized tests are tricky. JUnit's {@link Parameterized} runner is
 * notoriously bad at giving human-readable test case names (there are several
 * patch proposal on github but not applied to the trunk at the time of writing
 * this).
 * 
 * <p>{@link RandomizedRunner} has built-in support for parameters using a pair
 * of static-parameters provider method (factory) and a matching constructor.
 * The static method has to be public and annotated with {@link ParametersFactory},
 * as in {@link #parameters()} method below. Note the funky-looking <code>$</code>
 * and <code>$$</code> method which are static varargs collector methods to avoid
 * explicit array constructors.
 * <pre>
 * {@literal @}{@link ParametersFactory}
 * public static Iterable&lt;Object[]&gt; parameters() {
 *   return Arrays.asList($$(
 *     $(1, "abc"), 
 *     $(2, "def")));
 * }
 * </pre>
 * 
 * <p>The matching class constructor must declare type-assignable parameters. Because method
 * arguments are not part of the Java reflection, they can be explicitly annotated using
 * {@link Name} annotation to provide sensible names. 
 * The {@link #Test007ParameterizedTests(int, String)}
 * constructor shows an example of how this looks.
 * 
 * <p>If there is more than one set of parameters, method names will be postfixed with 
 * a list of parameters and their values. An additional <code>#num</code> identifier will
 * be added to make tests unique.
 * 
 * <p>{@link ParametersFactory} can be combined with other annotations such as 
 * {@link Repeat} or {@link Seeds} as shown in {@link #paramsWithRepeatAndSeeds()}.
 * 
 * <p>Note that {@code ParametersFactory.argumentFormatting()} permits custom 
 * test case naming, see the example factory in this class.
 */
public class Test007ParameterizedTests extends RandomizedTest {
  private int value;
  private String string;
  
  public Test007ParameterizedTests(
      @Name("value")  int value,
      @Name("string") String string) {
    this.value = value;
    this.string = string;
  }

  @Test
  public void simpleArgumentsConsumer() {
    System.out.println(value + " " + string + " "
        + getContext().getRandomness());
  }

  @Seeds({@Seed("deadbeef"), @Seed("cafebabe")})
  @Test
  @Repeat(iterations = 2, useConstantSeed = true)
  public void paramsWithRepeatAndSeeds() {
    System.out.println(value + " " + string + " "
        + getContext().getRandomness());
  }
  
  @ParametersFactory
  public static Iterable<Object[]> parameters() {
    return Arrays.asList($$(
        $(1, "abc"), 
        $(2, "def")));
  }
  
  /**
   * A factory with custom test name formatting. Note parameters
   * are reversed and referenced from the formatting string via
   * {@link Formatter} positional order syntax
   */
  @ParametersFactory(argumentFormatting = "param2:%2$s param1:%1$04d")
  public static Iterable<Object[]> parametersWithCustomName() {
    return Arrays.asList($$(
        $(3, "foo"), 
        $(4, "bar")));
  }  
}
