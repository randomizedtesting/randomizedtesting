package com.carrotsearch.ant.tasks.junit4;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;

/**
 * Duplicate nested resources N times. Useful for running
 * suites multiple times with load balancing.
 */
public class DuplicateResources extends DataType implements ResourceCollection {
  private final List<ResourceCollection> rcs = new ArrayList<ResourceCollection>();
  private int times; 
  
  public void addConfigured(ResourceCollection rc) {
    this.rcs.add(rc);
  }

  /**
   * Set the number of times the input resources should be duplicated.
   */
  public void setTimes(int times) {
    this.times = times;
  }

  @Override
  public Iterator<Resource> iterator() {
    return getElements().iterator();
  }

  private List<Resource> getElements() {
    List<Resource> elements = new ArrayList<>();
    for (ResourceCollection rc : rcs) {
      for (Resource r : rc) {
        for (int t = 0; t < times; t++) {
          elements.add(r);
        }
      }
    }
    return elements;
  }

  @Override
  public int size() {
    return getElements().size();
  }

  @Override
  public boolean isFilesystemOnly() {
    for (Iterator i = iterator(); i.hasNext();) {
      if (!((Resource) i.next()).isFilesystemOnly()) {
        return false;
      }
    }
    return true;
  }
}
