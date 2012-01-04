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
import com.carrotsearch.ant.tasks.junit4.events.json.*;
import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;
import com.carrotsearch.ant.tasks.junit4.listeners.AggregatedEventListener;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

/**
 * A report listener that produces a single JSON file for all suites and tests.
 */
public class JsonReport implements AggregatedEventListener {
  private JUnit4 junit4;
  private File targetFile;

  private String jsonpMethod;
  private JsonWriter jsonWriter;
  private Gson gson;

  private Map<Integer, SlaveInfo> slaves = Maps.newTreeMap();
  private OutputStreamWriter writer;

  /**
   * Output file for the report.
   */
  public void setFile(File file) {
    this.targetFile = file;
  }

  /**
   * Sets wrapper method name for JSONP. If set to non-empty
   * value, will change the output format to JSONP.
   * 
   * @see "http://en.wikipedia.org/wiki/JSONP"
   */
  public void setJsonpMethod(String method) {
    this.jsonpMethod = Strings.emptyToNull(method);
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

    final ClassLoader refLoader = Thread.currentThread().getContextClassLoader(); 
    this.gson = new GsonBuilder()
      .registerTypeAdapter(AggregatedSuiteResultEvent.class, new JsonAggregatedSuiteResultEventAdapter())
      .registerTypeAdapter(AggregatedTestResultEvent.class, new JsonAggregatedTestResultEventAdapter())
      .registerTypeAdapter(FailureMirror.class, new JsonFailureMirrorAdapter())
      .registerTypeAdapter(SlaveInfo.class, new JsonSlaveInfoAdapter())
      .registerTypeHierarchyAdapter(Annotation.class, new JsonAnnotationAdapter(refLoader))
      .registerTypeHierarchyAdapter(Class.class, new JsonClassAdapter(refLoader))
      .registerTypeAdapter(Description.class, new JsonDescriptionAdapter())
      .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS") // TODO: add second fractions here?
      .setPrettyPrinting().create();

    try {
      writer = new OutputStreamWriter(
          new BufferedOutputStream(new FileOutputStream(targetFile)),Charsets.UTF_8);
      
      if (!Strings.isNullOrEmpty(jsonpMethod)) {
        writer.write(jsonpMethod);
        writer.write("(");
      }

      jsonWriter = new JsonWriter(writer);
      jsonWriter.setHtmlSafe(false);
      jsonWriter.setIndent("  ");

      jsonWriter.beginObject(); // Main holder.

      // junit4 object with properties.
      jsonWriter.name("junit4");
      jsonWriter.beginObject();
      jsonWriter.name("tests.seed");
      jsonWriter.value(junit4.getSeed());
      jsonWriter.endObject();

      // suites and an array of suites follows.
      jsonWriter.name("suites");
      jsonWriter.beginArray();
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

      slaves.put(e.getSlave().id, e.getSlave());
      gson.toJson(e, e.getClass(), jsonWriter);
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
      jsonWriter.endArray();

      jsonWriter.name("slaves");
      gson.toJson(slaves, slaves.getClass(), jsonWriter);

      jsonWriter.endObject();
      jsonWriter.flush();

      if (!Strings.isNullOrEmpty(jsonpMethod)) {
        writer.write(");");
      }

      jsonWriter.close();
      jsonWriter = null;
      writer = null;
    } catch (IOException x) {
      // Ignore.
    }
  }
}
