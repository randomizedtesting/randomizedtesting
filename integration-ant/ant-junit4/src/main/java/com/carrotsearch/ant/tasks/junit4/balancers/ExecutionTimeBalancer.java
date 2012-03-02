package com.carrotsearch.ant.tasks.junit4.balancers;

import java.util.*;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ResourceCollection;

import com.carrotsearch.ant.tasks.junit4.Duration;
import com.carrotsearch.ant.tasks.junit4.JUnit4;
import com.carrotsearch.ant.tasks.junit4.SuiteBalancer;
import com.carrotsearch.ant.tasks.junit4.listeners.ExecutionTimesReport;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A test suite balancer based on past execution times saved using
 * {@link ExecutionTimesReport}.
 */
public class ExecutionTimeBalancer extends ProjectComponent implements SuiteBalancer {
  private static class SlaveLoad {
    public static final Comparator<SlaveLoad> ASCENDING_BY_ESTIMATED_FINISH = 
        new Comparator<SlaveLoad>() {
      @Override
      public int compare(SlaveLoad o1, SlaveLoad o2) {
        if (o1.estimatedFinish < o2.estimatedFinish) {
          return -1;
        } else if (o1.estimatedFinish == o2.estimatedFinish) {
          return o1.id - o2.id; // Assume no overflows.
        } else {
          return 1;
        }
      }
    };

    public final int id;
    public long estimatedFinish;

    public SlaveLoad(int id) {
      this.id = id;
    }
  }
  
  /**
   * All included execution time dumps.
   */
  private List<ResourceCollection> resources = Lists.newArrayList();
  
  /** Owning task (logging). */
  private JUnit4 owner;

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


  /**
   * Assign based on execution time history. The algorithm is a greedy heuristic
   * assigning the longest remaining test to the slave with the
   * shortest-completion time so far. This is not optimal but fast and provides
   * a decent average assignment.
   */
  @Override
  public LinkedHashMap<String,Assignment> assign(Collection<String> suiteNames, int slaves, long seed) {
    // Read hints first.
    final Map<String,List<Long>> hints = ExecutionTimesReport.mergeHints(resources, suiteNames);

    // Preprocess and sort costs. Take the median for each suite's measurements as the 
    // weight to avoid extreme measurements from screwing up the average.
    final List<SuiteHint> costs = Lists.newArrayList();
    for (Map.Entry<String,List<Long>> e : hints.entrySet()) {
      // Take the median for each suite's measurements as the weight
      // to avoid extreme measurements from screwing up the average.
      final List<Long> values = e.getValue();
      Collections.sort(values);
      final Long median = values.get(values.size() / 2);
      costs.add(new SuiteHint(e.getKey(), median));
    }
    Collections.sort(costs, SuiteHint.DESCENDING_BY_WEIGHT);

    // Apply the assignment heuristic.
    final PriorityQueue<SlaveLoad> pq = new PriorityQueue<SlaveLoad>(
        slaves, SlaveLoad.ASCENDING_BY_ESTIMATED_FINISH);
    for (int i = 0; i < slaves; i++) {
      pq.add(new SlaveLoad(i));
    }

    final LinkedHashMap<String, Assignment> assignments = Maps.newLinkedHashMap();
    for (SuiteHint hint : costs) {
      SlaveLoad slave = pq.remove();
      slave.estimatedFinish += hint.cost;
      pq.add(slave);

      owner.log("Expected execution time for " + hint.suiteName + ": " +
          Duration.toHumanDuration(hint.cost),
          Project.MSG_DEBUG);

      assignments.put(hint.suiteName, new Assignment(slave.id, (int) hint.cost));
    }
    
    // Dump estimated execution times.
    TreeMap<Long, SlaveLoad> ordered = new TreeMap<Long, SlaveLoad>();
    while (!pq.isEmpty()) {
      SlaveLoad slave = pq.remove();
      ordered.put(slave.estimatedFinish, slave);
    }
    for (SlaveLoad slave : ordered.values()) {
      owner.log(String.format(Locale.ENGLISH, 
          "Expected execution time on slave %d: %8.2fs",
          slave.id,
          slave.estimatedFinish / 1000.0f), Project.MSG_INFO);
    }

    return assignments;
  }


  @Override
  public void setOwner(JUnit4 owner) {
    this.owner = owner;
  }
}
