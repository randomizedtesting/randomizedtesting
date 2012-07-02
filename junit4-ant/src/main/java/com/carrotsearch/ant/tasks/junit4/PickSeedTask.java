package com.carrotsearch.ant.tasks.junit4;

import java.util.Random;

import org.apache.tools.ant.*;

import com.carrotsearch.randomizedtesting.SeedUtils;
import com.carrotsearch.randomizedtesting.SysGlobals;
import com.google.common.base.Strings;

/**
 * An ANT task to pick and fix the random seed in advance (for selecting
 * other derivative values, for example).
 */
public class PickSeedTask extends Task {

  /**
   * Name of the property to set.
   */
  private String propertyName;

  /**
   * Picks a random seed and writes it to a given property. If the property
   * is already defined nothing is done. 
   */
  public void setProperty(String propertyName) {
    this.propertyName = propertyName;
  }
  
  /**
   * Execute the task.
   */
  @Override
  public void execute() throws BuildException {
    validate();

    String seedValue = Strings.emptyToNull(getProject().getProperty(propertyName)); 
    if (seedValue == null) {
      seedValue = SeedUtils.formatSeed(new Random().nextLong());
      log("Picking master seed for property '" + propertyName + "': "
          + seedValue, Project.MSG_VERBOSE);
      getProject().setProperty(propertyName, seedValue);
    } else {
      log("Seed property '" + propertyName + "' already defined: "
          + seedValue, Project.MSG_INFO);
    }
  }

  /**
   * Validate arguments and state.
   */
  private void validate() {
    if (propertyName == null) {
      propertyName = SysGlobals.SYSPROP_RANDOM_SEED();
    }
  }
}
