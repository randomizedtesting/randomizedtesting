package com.carrotsearch.ant.tasks.junit4;

import static com.carrotsearch.randomizedtesting.SysGlobals.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.Task;

import com.carrotsearch.randomizedtesting.SeedUtils;
import com.carrotsearch.randomizedtesting.generators.RandomPicks;
import com.google.common.base.Strings;

/**
 * An ANT task to pick and fix the random seed in advance (for selecting
 * other derivative values, for example).
 */
public class PickFromListTask extends Task {
  /**
   * Nested value element.
   */
  public final static class StringValue extends ProjectComponent {
    private String value = "";

    public void addText(String value) {
        this.value += getProject().replaceProperties(value);
    }
    
    @Override
    public String toString() {
      return value;
    }
  }
  
  /**
   * Name of the property to set.
   */
  private String propertyName;

  /**
   * Allow the property to be undefined as one of 
   * the pick choices.
   */
  private boolean allowUndefined = false; 

  /**
   * Random seed to use. 
   */
  private String random;

  /**
   * Values to pick from.
   */
  private List<StringValue> values = new ArrayList<>();

  /**
   * Execution ID used to permute the pick order for lists of identical length
   * and identical seed.
   */
  private static AtomicInteger executionId = new AtomicInteger();

  public void setProperty(String propertyName) {
    this.propertyName = propertyName;
  }

  public void setAllowUndefined(boolean allowUndefined) {
    this.allowUndefined = allowUndefined;
  }

  public void setSeed(String randomSeed) {
    if (!Strings.isNullOrEmpty(getProject().getUserProperty(SYSPROP_RANDOM_SEED()))) {
      String userProperty = getProject().getUserProperty(SYSPROP_RANDOM_SEED());
      if (!userProperty.equals(randomSeed)) {
        log("Ignoring seed attribute because it is overridden by user properties.", Project.MSG_WARN);
      }
    } else if (!Strings.isNullOrEmpty(randomSeed)) {
      this.random = randomSeed;
    }
  }

  public StringValue createValue() {
    StringValue v = new StringValue();
    values.add(v);
    return v;
  }
  
  /**
   * Execute the task.
   */
  @Override
  public void execute() throws BuildException {
    validate();

    if (allowUndefined) {
      values.add(null);
    }

    long permutedSeed = SeedUtils.parseSeedChain(random)[0];
    permutedSeed ^= new Random(executionId.incrementAndGet()).nextLong();

    StringValue pick = RandomPicks.randomFrom(new Random(permutedSeed), values);
    if (pick != null) {
      getProject().setProperty(propertyName, pick.toString());
    }
  }

  /**
   * Validate arguments and state.
   */
  private void validate() {
    if (Strings.emptyToNull(random) == null) {
      random = Strings.emptyToNull(getProject().getProperty(SYSPROP_RANDOM_SEED())); 
    }
    
    if (random == null) {
      throw new BuildException("Required attribute 'seed' must not be empty. Look at <junit4:pickseed>.");
    }
    
    long[] seeds = SeedUtils.parseSeedChain(random);
    if (seeds.length < 1) {
      throw new BuildException("Random seed is required.");
    }
    
    if (values.isEmpty() && !allowUndefined) {
      throw new BuildException("No values to pick from and allowUndefined=false.");
    }
  }
}
