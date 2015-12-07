package com.carrotsearch.ant.tasks.junit4.listeners;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;

import com.carrotsearch.ant.tasks.junit4.JUnit4;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedQuitEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedSuiteResultEvent;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Closer;
import com.google.common.io.Files;

/**
 * A report listener that emits per-suite execution times information useful
 * for load balancing tests across JVMs. 
 */
public class ExecutionTimesReport implements AggregatedEventListener {
  /**
   * @see #setHistoryLength(int)
   */
  public final static int DEFAULT_HISTORY_LENGTH = 10;

  /**
   * The file where suite hints are stored/ updated.
   */
  private File hintsFile;

  /**
   * Execution time hints. Key: suite name, value: execution times.
   */
  private Map<String, List<Long>> hints; 

  /**
   * @see #setHistoryLength(int)
   */
  private int historyLength = DEFAULT_HISTORY_LENGTH;

  /**
   * Outer task.
   */
  private JUnit4 outer;
  
  /**
   * Hints file (key-value pairs).
   */
  public void setFile(File hintsFile) {
    this.hintsFile = hintsFile;
  }
  
  /**
   * How many execution times to store per-suite? The history must be larger than 0.
   */
  public void setHistoryLength(int length) {
    if (length < 0) {
      throw new BuildException("History length must be >= 1: " + length);
    }
    this.historyLength = length;
  }

  /**
   * Remember execution time for all executed suites.
   */
  @Subscribe
  public void onSuiteResult(AggregatedSuiteResultEvent e) {
    long millis = e.getExecutionTime();
    String suiteName = e.getDescription().getDisplayName();
    
    List<Long> values = hints.get(suiteName);
    if (values == null) {
      hints.put(suiteName, values = new ArrayList<>());
    }
    values.add(millis);
    while (values.size() > historyLength)
      values.remove(0);
  }
  
  /**
   * Write back to hints file.
   */
  @Subscribe
  public void onEnd(AggregatedQuitEvent e) {
    try {
      writeHints(hintsFile, hints);
    } catch (IOException exception) {
      outer.log("Could not write back the hints file.", exception, Project.MSG_ERR);
    }
  }

  @Override
  public void setOuter(JUnit4 outer) throws BuildException {
    if (hintsFile == null) {
      throw new BuildException(
          "Execution times listener requires file attribute.");
    }
    
    // If the file already exists, read its contents.
    try {
      if (hintsFile.isFile()) {
          hints = readHints(hintsFile);
      } else {
          if (!hintsFile.createNewFile()) {
            throw new BuildException("Could not create file: "
                + hintsFile.getAbsolutePath());
          }
          hints = new HashMap<>();
      }
    } catch (IOException e) {
      throw new BuildException("Could not read or create hints file: " + hintsFile.getAbsolutePath(), e);
    }
    
    this.outer = outer;
  }

  /**
   * Read hints from a file.
   */
  public static Map<String,List<Long>> readHints(File hints) throws IOException {
    Map<String,List<Long>> result = new HashMap<>();
    InputStream is = new FileInputStream(hints);
    mergeHints(is, result);
    return result;
  }

  /**
   * Read hints from a file and merge with the given hints map.
   */
  public static void mergeHints(InputStream is, Map<String,List<Long>> hints) throws IOException {
    final BufferedReader reader = new BufferedReader(
        new InputStreamReader(is, Charsets.UTF_8));
    
    String line;
    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (line.isEmpty() || line.startsWith("#"))
        continue;

      final int equals = line.indexOf('=');
      if (equals <= 0) {
        throw new IOException("No '=' character on a non-comment line?: " + line);
      } else {
        String key = line.substring(0, equals);
        List<Long> values = hints.get(key);
        if (values == null) {
          hints.put(key, values = new ArrayList<>());
        }
        for (String v : line.substring(equals + 1).split("[\\,]")) {
          if (!v.isEmpty()) values.add(Long.parseLong(v));
        }
      }
    }
  }

  /**
   * Writes back hints file. 
   */
  public static void writeHints(File file, Map<String,List<Long>> hints) throws IOException {
    Closer closer = Closer.create();
    try {
      BufferedWriter w = closer.register(Files.newWriter(file, Charsets.UTF_8));
      if (!(hints instanceof SortedMap)) {
        hints = new TreeMap<String,List<Long>>(hints);
      }
      
      Joiner joiner = Joiner.on(',');
      for (Map.Entry<String,List<Long>> e : hints.entrySet()) {
        w.write(e.getKey());
        w.write("=");
        joiner.appendTo(w, e.getValue());
        w.write("\n");
      }
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
  }

  /**
   * Read hints from all resources in a collection, retaining 
   * <code>suiteNames</code>. If <code>suiteNames</code> is null,
   * everything is retained.
   */
  public static Map<String,List<Long>> mergeHints(
      Collection<ResourceCollection> resources, Collection<String> suiteNames) {
    final Map<String,List<Long>> hints = new HashMap<>();
    for (ResourceCollection rc : resources) {
      Iterator<Resource> i = rc.iterator();
      while (i.hasNext()) {
        InputStream is = null;
        Resource r = i.next();
        try {
          is = r.getInputStream();
          mergeHints(is, hints);

          // Early prune the hints to those we have on the list.
          if (suiteNames != null) {
            hints.keySet().retainAll(suiteNames);
          }
        } catch (IOException e) {
          throw new BuildException("Could not read hints from resource: "
              + r.getDescription(), e);
        } finally {
          try {
            if (is != null) is.close();
          } catch (IOException e) {
            throw new BuildException("Could not close hints file: " + r.getDescription());
          }
        }
      }
    }
    return hints;
  }  
}
