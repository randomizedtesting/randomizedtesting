package com.carrotsearch.ant.tasks.junit4.listeners.json;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.google.common.io.Files;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import com.carrotsearch.ant.tasks.junit4.ForkedJvmInfo;
import com.carrotsearch.ant.tasks.junit4.JUnit4;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedQuitEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedSuiteResultEvent;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonWriter;
import com.carrotsearch.ant.tasks.junit4.listeners.AggregatedEventListener;
import com.carrotsearch.randomizedtesting.annotations.SuppressForbidden;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Resources;

/**
 * A report listener that produces a single JSON file for all suites and tests.
 */
public class JsonReport implements AggregatedEventListener {
  private JUnit4 junit4;
  private File targetFile;

  private String jsonpMethod;
  private JsonWriter jsonWriter;

  private String projectName;
  
  private Map<Integer, ForkedJvmInfo> slaves = new TreeMap<>();
  private OutputStreamWriter writer;

  private static enum OutputMethod {
    JSON,
    JSONP,
    HTML
  }

  /**
   * How should the report be written?
   */
  private OutputMethod method;
  
  /**
   * @see #setOutputStreams(boolean)
   */
  private boolean outputStreams = true;

  /**
   * Output file for the report file. The name of the output file
   * will also trigger how the report is written. If the name of the
   * output file ends with ".htm(l)?" then the output file is a HTML
   * file and CSS/JS scaffolding is also written to visualize the JSON
   * model.
   * 
   * If the name of the file ends with ".json(p)?" a JSON file is written. 
   */
  public void setFile(File file) {
    String fileName = file.getName().toLowerCase(Locale.ROOT);
    if (fileName.matches(".*\\.htm(l)?$")) {
      method = OutputMethod.HTML;
    } else {
      if (fileName.matches(".*\\.jsonp")) {
        method = OutputMethod.JSONP;
      } else {
        method = OutputMethod.JSON;
      }
    }
    this.targetFile = file;
  }

  /**
   * Sets wrapper method name for JSONP. If set to non-empty
   * value, will change the output format to JSONP. The name of the
   * JSONP function for the HTML wrapper must be "testData".
   * 
   * @see "http://en.wikipedia.org/wiki/JSONP"
   */
  public void setJsonpMethod(String method) {
    this.jsonpMethod = Strings.emptyToNull(method);
  }

  /**
   * Set project name for the output model.
   */
  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  /**
   * Include output streams? Mind that with large outputs the report may OOM.
   */
  public void setOutputStreams(boolean outputStreams) {
    this.outputStreams = outputStreams;
  }

  /*
   * 
   */
  @Override
  public void setOuter(JUnit4 junit4) {
    this.junit4 = junit4;
   
    if (this.targetFile == null) {
      throw new BuildException("'file' attribute is required (target file name ending in .html, .json or .jsonp).");
    }

    if (method == OutputMethod.HTML) {
      if (Strings.isNullOrEmpty(jsonpMethod)) {
        setJsonpMethod("testData");
      } else if (!"testData".equals(jsonpMethod)) {
        throw new BuildException("JSONP method must be empty or equal 'testData' for HTML output.");
      }
    }
    
    if (method == OutputMethod.JSONP) {
      if (Strings.isNullOrEmpty(jsonpMethod)) {
        setJsonpMethod("testData");
      }
    }
    
    try {
      Files.createParentDirs(targetFile);

      File jsonFile = targetFile;
      if (method == OutputMethod.HTML) {
        jsonFile = new File(removeExtension(targetFile.getAbsolutePath()) + ".jsonp");
      }

      writer = new OutputStreamWriter(
          new BufferedOutputStream(new FileOutputStream(jsonFile)),Charsets.UTF_8);
      
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
      jsonWriter.name("project.name");
      jsonWriter.value(getProjectName());
      jsonWriter.endObject();

      // suites and an array of suites follows.
      jsonWriter.name("suites");
      jsonWriter.beginArray();
    } catch (IOException e) {
      throw new BuildException("Could not emit JSON report.", e);
    }
  }

  private String removeExtension(String name) {
    if (name.indexOf(".") > 0) {
      name = name.substring(0, name.lastIndexOf("."));
    }
    return name;
  }

  /**
   * Return the project name or the default project name.
   */
  private String getProjectName() {
    String pName = Strings.emptyToNull(projectName);
    if (pName == null) { 
      pName = Strings.emptyToNull(junit4.getProject().getName());
    }
    if (pName == null) {
      pName = "(unnamed project)";      
    }
    return pName;
  }

  /**
   * Emit information about a single suite and all of its tests. 
   */
  @Subscribe
  @SuppressForbidden("legitimate printStackTrace().")
  public void onSuiteResult(AggregatedSuiteResultEvent e) {
    try {
      if (jsonWriter == null)
        return;

      slaves.put(e.getSlave().id, e.getSlave());
      e.serialize(jsonWriter, outputStreams);
    } catch (Exception ex) {
      ex.printStackTrace();
      junit4.log("Error serializing to JSON file: "
          + Throwables.getStackTraceAsString(ex), Project.MSG_WARN);
      if (jsonWriter != null) {
        try {
          jsonWriter.close();
        } catch (Throwable ignored) {
          // Ignore.
        } finally {
          jsonWriter = null;
        }
      }
    }
  }

  /**
   * All tests completed.
   */
  @Subscribe
  public void onQuit(AggregatedQuitEvent e) {
    if (jsonWriter == null)
      return;

    try {
      jsonWriter.endArray();

      jsonWriter.name("slaves");
      jsonWriter.beginObject();
      for (Map.Entry<Integer, ForkedJvmInfo> entry : slaves.entrySet()) {
        jsonWriter.name(Integer.toString(entry.getKey()));
        entry.getValue().serialize(jsonWriter);
      }
      jsonWriter.endObject();

      jsonWriter.endObject();
      jsonWriter.flush();

      if (!Strings.isNullOrEmpty(jsonpMethod)) {
        writer.write(");");
      }

      jsonWriter.close();
      jsonWriter = null;
      writer = null;

      if (method == OutputMethod.HTML) {
        copyScaffolding(targetFile);
      }
    } catch (IOException x) {
      junit4.log(x, Project.MSG_ERR);
    }
  }

  /**
   * Copy HTML/JS/CSS scaffolding to a targetFile's directory.
   */
  private void copyScaffolding(File targetFile) throws IOException {
    String resourcePrefix = "com/carrotsearch/ant/tasks/junit4/templates/json/";

    File parent = targetFile.getParentFile();

    // Handle index.html substitutitons.
    ClassLoader cl = this.getClass().getClassLoader();
    String index = 
        Resources.toString(
            cl.getResource(resourcePrefix + "index.html"), Charsets.UTF_8);
    index = index.replaceAll(Pattern.quote("tests-output.jsonp"),
        removeExtension(targetFile.getName()) + ".jsonp");

    Files.write(index.getBytes(Charsets.UTF_8), targetFile);
    
    // Copy over the remaining files. This is hard coded but scanning a JAR seems like an overkill.
    String [] resources = {
        "js/jquery-1.7.1.min.js",
        "js/script.js",
        "js/jquery.pathchange.js",
        "img/pass.png",
        "img/error.png",
        "img/stderr.png",
        "img/arrow-up.png",
        "img/stdout.png",
        "img/indicator.png",
        "img/failure.png",
        "img/omited.png",
        "img/arrow-down.png",
        "css/style.css"
    };

    for (String resource : resources) {
      File target = new File(parent, resource);
      if (!target.getParentFile().exists()) {
        target.getParentFile().mkdirs();
      }
      URL res = cl.getResource(resourcePrefix + resource);
      if (res == null) {
        throw new IOException("Could not find the required report resource: " + resource);
      }
      Files.write(Resources.toByteArray(res), target); 
    }
  }
}
