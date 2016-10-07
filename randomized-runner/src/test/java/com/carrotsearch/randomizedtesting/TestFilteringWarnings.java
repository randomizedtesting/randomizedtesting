package com.carrotsearch.randomizedtesting;

import java.util.logging.*;

import org.junit.*;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Filterable;

import com.carrotsearch.randomizedtesting.rules.SystemPropertiesRestoreRule;

/**
 * Test warnings resulting from potential misuse of {@link Filterable} interface.
 */
public class TestFilteringWarnings extends WithNestedTestClass {
  @Rule
  public SystemPropertiesRestoreRule restoreProperties = new SystemPropertiesRestoreRule(); 
  
  private Logger rootLogger; 
  private Handler[] handlers;
  private StringBuilder buffer = new StringBuilder();

  private Handler bufferingHandler = new Handler() {
    @Override
    public void publish(LogRecord record) {
      buffer.append(record.getMessage());
    }

    @Override
    public void flush() {}
    
    @Override
    public void close() throws SecurityException {}
  };
  
  /**
   * Test class.
   */
  public static class Nested extends RandomizedTest {
    @Test
    public void method() {
      // Just in case...
      Assume.assumeTrue(getContext().getRunnerSeed() != 0x614CDBEAE7160809L);
    }
  }

  @Before
  public void setup() throws Exception {
    // Attach to the root logger.
    LogManager logManager = LogManager.getLogManager();
    rootLogger = logManager.getLogger("");
    for (Handler h : (handlers = rootLogger.getHandlers())) {
      rootLogger.removeHandler(h);
    }
    rootLogger.addHandler(bufferingHandler);
  }

  @After
  public void cleanup() {
    buffer.setLength(0);
    rootLogger.removeHandler(bufferingHandler);
    for (Handler h : handlers) {
      rootLogger.addHandler(h);
    }
  }

  /**
   * Filter contains a seed but random seed will generate unique repetitions.
   */
  @Test
  public void testNoIters() {
    System.setProperty(SysGlobals.SYSPROP_TESTMETHOD(), "method");
    System.setProperty(SysGlobals.SYSPROP_RANDOM_SEED(), "deadbeef");
    Result result = runClasses(Nested.class);
    if (result.getIgnoreCount() == 0) {
      Assert.assertTrue(result.getFailures().isEmpty());
      Assert.assertTrue(getOutput().isEmpty());
    }
  }

  private String getOutput() {
    return buffer.toString();
  }
}
