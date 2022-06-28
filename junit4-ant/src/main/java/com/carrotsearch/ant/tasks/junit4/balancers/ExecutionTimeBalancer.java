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
  private static class ForkedJvmLoad {
    public static final Comparator<ForkedJvmLoad> ASCENDING_BY_ESTIMATED_FINISH = new Comparator<ForkedJvmLoad>() {
      @Override
      public int compare(ForkedJvmLoad o1, ForkedJvmLoad o2) {
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

    public ForkedJvmLoad(int id) {
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
   * assigning the longest remaining test to the forked JVM with the
   * shortest-completion time so far. This is not optimal but fast and provides
   * a decent average assignment.
   */
  @Override
  public List<Assignment> assign(Collection<String> suiteNames, int forkedJvmCount, long seed) {
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
    final PriorityQueue<ForkedJvmLoad> pq = new PriorityQueue<ForkedJvmLoad>(
        forkedJvmCount, ForkedJvmLoad.ASCENDING_BY_ESTIMATED_FINISH);
    for (int i = 0; i < forkedJvmCount; i++) {
      pq.add(new ForkedJvmLoad(i));
    }

    final List<Assignment> assignments = new ArrayList<>();
    for (SuiteHint hint : costs) {
      ForkedJvmLoad forkedJvm = pq.remove();
      forkedJvm.estimatedFinish += hint.cost;
      pq.add(forkedJvm);

      owner.log("Expected execution time for " + hint.suiteName + ": " +
          Duration.toHumanDuration(hint.cost),
          Project.MSG_DEBUG);

      assignments.add(new Assignment(hint.suiteName, forkedJvm.id, (int) hint.cost));
    }

    // Dump estimated execution times.
    TreeMap<Integer, ForkedJvmLoad> ordered = new TreeMap<Integer, ForkedJvmLoad>();
    while (!pq.isEmpty()) {
      ForkedJvmLoad forkedJvmLoad = pq.remove();
      ordered.put(forkedJvmLoad.id, forkedJvmLoad);
    }
    for (Integer id : ordered.keySet()) {
      final ForkedJvmLoad forkedJvmLoad = ordered.get(id);
      owner.log(String.format(Locale.ROOT, 
          "Expected execution time on JVM J%d: %8.2fs",
          forkedJvmLoad.id,
          forkedJvmLoad.estimatedFinish / 1000.0f),
          verbose ? Project.MSG_INFO : Project.MSG_DEBUG);
    }

    return assignments;
  }


  @Override
  public void setOwner(JUnit4 owner) {
    this.owner = owner;
  }
}
