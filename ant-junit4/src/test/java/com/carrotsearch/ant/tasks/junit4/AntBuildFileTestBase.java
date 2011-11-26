package com.carrotsearch.ant.tasks.junit4;

import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.junit.Assert;

import com.google.common.base.Charsets;

/**
 * An equivalent of {@link BuildFileTest} for JUnit4.
 */
public class AntBuildFileTestBase {
  private Project project;
  private ByteArrayOutputStream output;
  private DefaultLogger listener;
  
  protected void setupProject(File projectFile) {
    project = new Project();
    project.init();

    project.setUserProperty("ant.file", projectFile.getAbsolutePath());
    ProjectHelper.configureProject(project, projectFile);

    output = new ByteArrayOutputStream();
    try {
      PrintStream ps = new PrintStream(output, true, Charsets.UTF_8.displayName());
      listener = new DefaultLogger();
      listener.setMessageOutputLevel(Project.MSG_DEBUG);
      listener.setErrorPrintStream(ps);
      listener.setOutputPrintStream(ps);
      getProject().addBuildListener(listener);
      
      DefaultLogger console = new DefaultLogger();
      console.setMessageOutputLevel(Project.MSG_INFO);
      console.setErrorPrintStream(System.err);
      console.setOutputPrintStream(System.out);
      getProject().addBuildListener(console);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected final Project getProject() {
    if (project == null) {
      throw new IllegalStateException(
          "Setup project file with setupProject(File) first.");
    }
    return project;
  }
  
  protected final void assertLogContains(String substring) {
    Assert.assertTrue("Log did not contain: '" + substring + "'", getLog()
        .contains(substring));
  }
  
  protected final String getLog() {
    try {
      return new String(output.toByteArray(), "UTF-8");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected final void expectBuildExceptionContaining(String target,
      String message) {
        try {
          getProject().executeTarget(target);
          Assert.fail("Expected a build failure with message: " + message);
        } catch (BuildException e) {
          Assert.assertThat(e.getMessage(), containsString(message));
        }
      }
}
