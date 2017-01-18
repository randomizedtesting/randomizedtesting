package com.carrotsearch.ant.tasks.junit4.balancers;

import java.util.*;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ResourceCollection;

import com.carrotsearch.ant.tasks.junit4.*;
import com.carrotsearch.ant.tasks.junit4.listeners.ExecutionTimesReport;

/**
 * A test suite balancer based on past execution times saved using
 * {@link ExecutionTimesReport}.
 */
public class ExecutionTimeBalancer extends ProjectComponent implements SuiteBalancer {
  private static class SlaveLoad {
    public static final Comparator<SlaveLoad> ASCENDING_BY_ESTIMATED_FINISH = new Comparator<SlaveLoad>() {
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
  private List<ResourceCollection> resources = new ArrayList<>();

  /** Owning task (logging). */
  private JUnit4 owner;

  /** @see #setVerbose(boolean) */
  private boolean verbose;

  /**
   * Be verbose about estimated times etc.
   */
  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
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


  /**
   * Assign based on execution time history. The algorithm is a greedy heuristic
   * assigning the longest remaining test to the slave with the
   * shortest-completion time so far. This is not optimal but fast and provides
   * a decent average assignment.
   */
  @Override
  public List<Assignment> assign(Collection<String> suiteNames, int slaves, long seed) {
    // Read hints first.
    final Map<String,List<Long>> hints = ExecutionTimesReport.mergeHints(resources, suiteNames);

    // Preprocess and sort costs. Take the median for each suite's measurements as the 
    // weight to avoid extreme measurements from screwing up the average.
    final List<SuiteHint> costs = new ArrayList<>();
    for (String suiteName : suiteNames) {
      final List<Long> suiteHint = hints.get(suiteName);
      if (suiteHint != null) {
        // Take the median for each suite's measurements as the weight
        // to avoid extreme measurements from screwing up the average.
        Collections.sort(suiteHint);
        final Long median = suiteHint.get(suiteHint.size() / 2);
        costs.add(new SuiteHint(suiteName, median));
      }
    }
    Collections.sort(costs, SuiteHint.DESCENDING_BY_WEIGHT);

    // Apply the assignment heuristic.
    final PriorityQueue<SlaveLoad> pq = new PriorityQueue<SlaveLoad>(
        slaves, SlaveLoad.ASCENDING_BY_ESTIMATED_FINISH);
    for (int i = 0; i < slaves; i++) {
      pq.add(new SlaveLoad(i));
    }

    final List<Assignment> assignments = new ArrayList<>();
    for (SuiteHint hint : costs) {
      SlaveLoad slave = pq.remove();
      slave.estimatedFinish += hint.cost;
      pq.add(slave);

      owner.log("Expected execution time for " + hint.suiteName + ": " +
          Duration.toHumanDuration(hint.cost),
          Project.MSG_DEBUG);

      assignments.add(new Assignment(hint.suiteName, slave.id, (int) hint.cost));
    }

    // Dump estimated execution times.
    TreeMap<Integer, SlaveLoad> ordered = new TreeMap<Integer, SlaveLoad>();
    while (!pq.isEmpty()) {
      SlaveLoad slave = pq.remove();
      ordered.put(slave.id, slave);
    }
    for (Integer id : ordered.keySet()) {
      final SlaveLoad slave = ordered.get(id);
      owner.log(String.format(Locale.ROOT, 
          "Expected execution time on JVM J%d: %8.2fs",
          slave.id,
          slave.estimatedFinish / 1000.0f), 
          verbose ? Project.MSG_INFO : Project.MSG_DEBUG);
    }

    return assignments;
  }


  @Override
  public void setOwner(JUnit4 owner) {
    this.owner = owner;
  }
}
