package com.carrotsearch.ant.tasks.junit4.balancers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ResourceCollection;

import com.carrotsearch.ant.tasks.junit4.listeners.ExecutionTimesReport;

/**
 * Merge execution hints emitted by {@link ExecutionTimeBalancer} to one file.
 */
public class MergeHints extends Task {
  /**
   * All included execution time dumps.
   */
  private List<ResourceCollection> resources = new ArrayList<>();

  /**
   * @see ExecutionTimesReport#setHistoryLength(int)
   */
  private int historyLength = ExecutionTimesReport.DEFAULT_HISTORY_LENGTH;

  /**
   * Output file for merged hints.
   */
  private File file;
  
  /**
   * @see ExecutionTimesReport#setHistoryLength(int)
   */
  public void setHistoryLength(int historyLength) {
    if (historyLength < 0) {
      throw new BuildException("History length must be >= 1: " + historyLength);
    }
    this.historyLength = historyLength;
  }

  /**
   * Set the output file for merged hints.
   */
  public void setFile(File file) {
    this.file = file;
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

    for (List<Long> hintList : hints.values()) {
      while (hintList.size() > historyLength) {
        hintList.remove(0);
      }
    }

    try {
      ExecutionTimesReport.writeHints(file, hints);
    } catch (IOException e) {
      throw new BuildException("Could not write updated hints file: " + file, e);
    }
  }
}
