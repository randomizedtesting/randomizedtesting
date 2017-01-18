package com.carrotsearch.ant.tasks.junit4.balancers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ResourceCollection;

import com.carrotsearch.ant.tasks.junit4.listeners.ExecutionTimesReport;

/**
 * Display the slowest test suites based on hints files.
 */
public class TopHints extends Task {

  private static class Entry {
    String suiteName;
    double averageHint;
    
    public Entry(String s, double h) {
      this.suiteName = s;
      this.averageHint = h;
    }
  }

  private static final Comparator<Entry> byDescHint = new Comparator<Entry>() {
    @Override
    public int compare(Entry o1, Entry o2) {
      if (o1.averageHint < o2.averageHint) return 1;
      if (o1.averageHint > o2.averageHint) return -1;
      return o1.suiteName.compareTo(o2.suiteName);
    }
  };

  /**
   * All included execution time dumps.
   */
  private List<ResourceCollection> resources = new ArrayList<>();

  /**
   * Max entries to display.
   */
  private int max = 10; 
  
  /**
   * The number of entries to display, maximum.
   */
  public void setMax(int maxEntries) {
    this.max = maxEntries;
  }

  /**
   * Adds a resource collection with execution hints.
   */
  public void add(ResourceCollection rc) {
    if (rc instanceof FileSet) {
      FileSet fs = (FileSet) rc;
      fs.setProject(getProject());
    }
    resources.add(rc);
  }

  @Override
  public void execute() throws BuildException {
    // Read hints first, preserve all hints.
    final Map<String,List<Long>> hints = ExecutionTimesReport.mergeHints(
        resources, /* keep all */ null);

    // Could be done with a pq without sorting everything...
    ArrayList<Entry> entries = new ArrayList<Entry>();
    for (Map.Entry<String,List<Long>> e : hints.entrySet()) {
      double average = 0;
      for (Long v : e.getValue()) {
        average += v;
      }
      entries.add(new Entry(e.getKey(), average / e.getValue().size()));
    }
    
    Collections.sort(entries, byDescHint);
    final int j = Math.min(max, entries.size());
    for (int i = 0; i < j; i++) {
      log(String.format(Locale.ROOT, "%6.2fs | %s", 
          entries.get(i).averageHint / 1000.0,
          entries.get(i).suiteName));
    }
  }
}
