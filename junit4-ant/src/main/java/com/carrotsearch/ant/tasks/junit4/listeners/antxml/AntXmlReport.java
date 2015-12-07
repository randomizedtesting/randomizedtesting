package com.carrotsearch.ant.tasks.junit4.listeners.antxml;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.filters.TokenFilter;
import org.junit.runner.Description;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.transform.RegistryMatcher;

import com.carrotsearch.ant.tasks.junit4.JUnit4;
import com.carrotsearch.ant.tasks.junit4.TestsSummaryEventListener;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedQuitEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedSuiteResultEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedTestResultEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.TestStatus;
import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;
import com.carrotsearch.ant.tasks.junit4.listeners.AggregatedEventListener;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

/**
 * A report listener that produces XML files compatible with those produced by
 * ANT's default <code>junit</code> task. These files do not include full
 * information but many tools can parse them.
 */
public class AntXmlReport implements AggregatedEventListener {
  private JUnit4 junit4;
  private File dir;
  private boolean mavenExtensions = true;
  private File summaryFile;
  private List<TokenFilter> filters = new ArrayList<>();
  private Map<String,Integer> suiteCounts = new HashMap<>();
  private boolean ignoreDuplicateSuites;
  
  /**
   * @see #setOutputStreams(boolean)
   */
  private boolean outputStreams = true;
  
  private final TestsSummaryEventListener summaryListener = new TestsSummaryEventListener();

  /**
   * Output directory to write reports to.
   */
  public void setDir(File dir) {
    this.dir = dir;
  }
  
  /**
   * Where to emit Maven's summary file? This can be used
   * by <a href="http://maven.apache.org/surefire/maven-failsafe-plugin/verify-mojo.html#summaryFile">
   * the failsafe plugin</a> to verify whether the build succeeded or not.  
   */
  public void setSummaryFile(File file) {
    this.summaryFile = file;
  }
  
  /**
   * Include output streams? Mind that with large outputs the report may OOM.
   */
  public void setOutputStreams(boolean outputStreams) {
    this.outputStreams = outputStreams;
  }
  
  /**
   * Emit maven elements in the XML (extensions compared to ANT).
   */
  public void setMavenExtensions(boolean mavenExtensions) {
    this.mavenExtensions = mavenExtensions;
  }

  /**
   * Ignore duplicate suite names.
   */
  public void setIgnoreDuplicateSuites(boolean ignoreDuplicateSuites) {
    this.ignoreDuplicateSuites = ignoreDuplicateSuites;
  }

  /**
   * Adds method name filter.
   */
  public void addConfiguredTokenFilter(TokenFilter filter) {
    this.filters.add(filter);
  }

  /*
   * 
   */
  @Override
  public void setOuter(JUnit4 junit4) {
    this.junit4 = junit4;
    
    if (this.dir == null) {
      throw new BuildException("'dir' attribute is required (target folder for reports).");
    }
    
    try {
      Files.createParentDirs(dir);
      if (!dir.exists()) {
        if (!dir.mkdir()) {
          throw new IOException("Could not mkdir: " + dir);
        }
      }
    } catch (IOException e) {
      throw new BuildException("Could not create parent folders of: " + dir, e);
    }
    
    try {
      if (summaryFile != null) {
        Files.createParentDirs(summaryFile);
      }
    } catch (IOException e) {
      throw new BuildException("Could not create parent folders of: " + summaryFile, e);
    }
  }

  /**
   * Write the summary file, if requested.
   */
  @Subscribe
  public void onQuit(AggregatedQuitEvent e) {
    if (summaryFile != null) {
      try {
        Persister persister = new Persister();
        persister.write(new MavenFailsafeSummaryModel(summaryListener.getResult()), summaryFile);
      } catch (Exception x) {
        junit4.log("Could not serialize summary report.", x, Project.MSG_WARN);
      }
    }
  }
  
  /**
   * Emit information about all of suite's tests. 
   */
  @Subscribe
  public void onSuiteResult(AggregatedSuiteResultEvent e) {
    // Calculate summaries.
    summaryListener.suiteSummary(e);

    Description suiteDescription = e.getDescription();
    String displayName = suiteDescription.getDisplayName();
    if (displayName.trim().isEmpty()) {
      junit4.log("Could not emit XML report for suite (null description).", 
          Project.MSG_WARN);
      return;
    }

    if (!suiteCounts.containsKey(displayName)) {
      suiteCounts.put(displayName, 1);
    } else {
      int newCount = suiteCounts.get(displayName) + 1;
      suiteCounts.put(displayName, newCount);
      if (!ignoreDuplicateSuites && newCount == 2) {
        junit4.log("Duplicate suite name used with XML reports: "
            + displayName + ". This may confuse tools that process XML reports. "
            + "Set 'ignoreDuplicateSuites' to true to skip this message.", Project.MSG_WARN);
      }
      displayName = displayName + "-" + newCount;
    }
    
    try {
      File reportFile = new File(dir, "TEST-" + displayName + ".xml");
      RegistryMatcher rm = new RegistryMatcher();
      rm.bind(String.class, new XmlStringTransformer());
      Persister persister = new Persister(rm);
      persister.write(buildModel(e), reportFile);
    } catch (Exception x) {
      junit4.log("Could not serialize report for suite "
          + displayName + ": " + x.toString(), x, Project.MSG_WARN);
    }
  }
  
  /**
   * Build data model for serialization.
   */
  private TestSuiteModel buildModel(AggregatedSuiteResultEvent e) throws IOException {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);
    TestSuiteModel suite = new TestSuiteModel();

    suite.hostname = "nohost.nodomain";
    suite.name = e.getDescription().getDisplayName();
    suite.properties = buildModel(e.getSlave().getSystemProperties());
    suite.time = e.getExecutionTime() / 1000.0;
    suite.timestamp = df.format(new Date(e.getStartTimestamp()));

    suite.testcases = buildModel(e.getTests());
    suite.tests = suite.testcases.size();

    if (mavenExtensions) {
      suite.skipped = 0;
    }

    // Suite-level failures and errors are simulated as test cases.
    for (FailureMirror m : e.getFailures()) {
      TestCaseModel model = new TestCaseModel();
      model.classname = "junit.framework.TestSuite"; // empirical ANT output.
      model.name = applyFilters(m.getDescription().getClassName());
      model.time = 0;
      if (m.isAssertionViolation()) {
        model.failures.add(buildModel(m));
      } else {
        model.errors.add(buildModel(m));
      }
      suite.testcases.add(model);
    }

    // Calculate test numbers that match limited view (no ignored tests, 
    // faked suite-level errors).
    for (TestCaseModel tc : suite.testcases) {
      suite.errors += tc.errors.size();
      suite.failures += tc.failures.size();
      if (mavenExtensions && tc.skipped != null) {
        suite.skipped += 1;
      }
    }

    StringWriter sysout = new StringWriter();
    StringWriter syserr = new StringWriter();
    if (outputStreams) {
      e.getSlave().decodeStreams(e.getEventStream(), sysout, syserr);
    }
    suite.sysout = sysout.toString();
    suite.syserr = syserr.toString();

    return suite;
  }

  /* */
  private List<TestCaseModel> buildModel(List<AggregatedTestResultEvent> testEvents) {
    List<TestCaseModel> tests = new ArrayList<>();
    for (AggregatedTestResultEvent e : testEvents) {
      TestCaseModel model = new TestCaseModel();

      if (e.getStatus() == TestStatus.IGNORED ||
          e.getStatus() == TestStatus.IGNORED_ASSUMPTION) {
        if (mavenExtensions) {
          // This emits an empty <skipped /> element.
          model.skipped = "";
        } else {
          // No way to report these in pure ANT XML.
          continue;
        }
      }

      model.name = applyFilters(e.getDescription().getMethodName());
      model.classname = e.getDescription().getClassName();
      model.time = e.getExecutionTime() / 1000.0;

      for (FailureMirror m : e.getFailures()) {
        if (m.isAssumptionViolation()) {
          // Assumptions are not represented in ANT or Maven XMLs.
          continue;
        } else if (m.isAssertionViolation()) {
          model.failures.add(buildModel(m));
        } else {
          model.errors.add(buildModel(m));
        }
      }

      tests.add(model);
    }
    return tests;
  }

  /**
   * Apply filters to a method name.
   * @param methodName
   */
  private String applyFilters(String methodName) {
    if (filters.isEmpty()) {
      return methodName;
    }

    Reader in = new StringReader(methodName);
    for (TokenFilter tf : filters) {
      in = tf.chain(in);
    }

    try {
      return CharStreams.toString(in);
    } catch (IOException e) {
      junit4.log("Could not apply filters to " + methodName + 
          ": " + Throwables.getStackTraceAsString(e), Project.MSG_WARN);
      return methodName;
    }
  }

  /* */
  private FailureModel buildModel(FailureMirror f) {
    FailureModel model = new FailureModel();
    model.message = Strings.nullToEmpty(f.getMessage());
    model.text = f.getTrace();
    model.type = f.getThrowableClass();
    return model;
  }

  /* */
  private List<PropertyModel> buildModel(Map<String,String> properties) {
    List<PropertyModel> props = new ArrayList<>();
    for (Map.Entry<String,String> e : properties.entrySet()) {
      props.add(new PropertyModel(e.getKey(), e.getValue()));
    }
    return props;
  }
}
