package com.carrotsearch.ant.tasks.junit4.listeners.json;

import java.io.*;
import java.lang.annotation.Annotation;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.JUnit4;
import com.carrotsearch.ant.tasks.junit4.SlaveInfo;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.*;
import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;
import com.carrotsearch.ant.tasks.junit4.listeners.AggregatedEventListener;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * A report listener that produces a single JSON file for all suites and tests.
 */
public class JsonReport implements AggregatedEventListener {
  private JUnit4 junit4;
  private File targetFile;
  
  private OutputStreamWriter output;
  private boolean firstSuite = true;
  private Gson gson;

  private Map<Integer, SlaveInfo> slaves = Maps.newTreeMap();

  /**
   * Output file for the report.
   */
  public void setFile(File file) {
    this.targetFile = file;
  }

  /*
   * 
   */
  @Override
  public void setOuter(JUnit4 junit4) {
    this.junit4 = junit4;
   
    if (this.targetFile == null) {
      throw new BuildException("'file' attribute is required (target file for JSON).");
    }

    this.gson = new GsonBuilder()
      .registerTypeAdapter(AggregatedSuiteResultEvent.class, new JsonSuiteResultEventAdapter())
      .registerTypeAdapter(AggregatedTestResultEvent.class, new JsonTestResultEventAdapter())
      .registerTypeAdapter(FailureMirror.class, new JsonFailureMirrorAdapter())
      .registerTypeAdapter(SlaveInfo.class, new JsonSlaveInfoAdapter())
      .registerTypeHierarchyAdapter(Annotation.class, new JsonAnnotationAdapter())
      .registerTypeHierarchyAdapter(Class.class, new JsonClassAdapter())      
      .registerTypeAdapter(Description.class, new JsonDescriptionAdapter())
      .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
      .setPrettyPrinting().create();

    try {
      this.output = new OutputStreamWriter(
          new BufferedOutputStream(new FileOutputStream(targetFile)), 
          Charsets.UTF_8);

      output.write("junit4 = ");
      JsonObject ob = new JsonObject();
      ob.addProperty("random", junit4.getSeed());
      gson.toJson(ob, output);
      output.write(";\n\n");

      output.write("suites = [\n\n");
    } catch (IOException e) {
      throw new BuildException("Could not emit JSON report.", e);
    }
  }

  /**
   * Emit information about a single suite and all of its tests. 
   */
  @Subscribe
  public void onSuiteResult(AggregatedSuiteResultEvent e) {
    try {
      if (gson == null)
        return;

      if (!firstSuite) {
        output.append(",\n\n");
      }
      firstSuite = false;

      slaves.put(e.getSlave().id, e.getSlave());
      gson.toJson(e, output);
    } catch (Exception ex) {
      junit4.log("Error serializing to JSON file: "
          + ex.toString(), ex, Project.MSG_WARN);
      gson = null;
    }
  }

  /**
   * All tests completed.
   */
  @Subscribe
  public void onQuit(AggregatedQuitEvent e) {
    try {
      output.write("\n\n];\n\n");

      output.write("slaves = ");
      gson.toJson(slaves, output);
      output.write(";");
      Closeables.closeQuietly(output);
    } catch (IOException x) {
      // Ignore.
    }
  }
}
