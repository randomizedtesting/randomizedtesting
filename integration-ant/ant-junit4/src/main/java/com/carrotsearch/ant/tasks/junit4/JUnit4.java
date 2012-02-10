package com.carrotsearch.ant.tasks.junit4;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.types.resources.Resources;
import org.apache.tools.ant.util.LoaderUtils;
import org.junit.runner.Description;
import org.objectweb.asm.ClassReader;

import com.carrotsearch.ant.tasks.junit4.balancers.RoundRobinBalancer;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.*;
import com.carrotsearch.ant.tasks.junit4.listeners.AggregatedEventListener;
import com.carrotsearch.ant.tasks.junit4.slave.SlaveMain;
import com.carrotsearch.ant.tasks.junit4.slave.SlaveMainSafe;
import com.carrotsearch.randomizedtesting.*;
import com.google.common.base.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.gson.Gson;

import static com.carrotsearch.randomizedtesting.SysGlobals.*;

/**
 * An ANT task to run JUnit4 tests. Differences (benefits?) compared to ANT's default JUnit task:
 * <ul>
 *  <li>Built-in parallel test execution support (spawns multiple JVMs to avoid 
 *  test interactions).</li>
 *  <li>Randomization of the order of test suites within a single JVM.</li>
 *  <li>Aggregates and synchronizes test events from executors. All reports run on
 *  the task's JVM (not on the test JVM).</li>
 *  <li>Fully configurable reporting via listeners (console, ANT-compliant XML, JSON).
 *  Report listeners use Google Guava's {@link EventBus} and receive full information
 *  about tests' execution (including skipped, assumption-skipped tests, streamlined
 *  output and error stream chunks, etc.).</li>
 *  <li>JUnit 4.10+ is required both for the task and for the tests classpath. 
 *  Older versions will cause build failure.</li>
 *  <li>Integration with {@link RandomizedRunner} (randomization seed is passed to
 *  children JVMs).</li>
 * </ul>
 */
public class JUnit4 extends Task {
  /** Name of the antlib resource inside JUnit4 JAR. */
  public static final String ANTLIB_RESOURCE_NAME = "junit4.antlib.xml";
  
  /** @see #setParallelism(String) */
  public static final Object PARALLELISM_AUTO = "auto";

  /** @see #setParallelism(String) */
  public static final String PARALLELISM_MAX = "max";

  /** Default value of {@link #setShuffleOnSlave}. */
  public static final boolean DEFAULT_SHUFFLE_ON_SLAVE = true;

  /** Default value of {@link #setParallelism}. */
  public static final String  DEFAULT_PARALLELISM = "1";

  /** Default value of {@link #setPrintSummary}. */
  public static final boolean DEFAULT_PRINT_SUMMARY = true;

  /** Default value of {@link #setHaltOnFailure}. */
  public static final boolean DEFAULT_HALT_ON_FAILURE = true;

  /**
   * Slave VM command line.
   */
  private CommandlineJava slaveCommand = new CommandlineJava();

  /**
   * Set new environment for the forked process?
   */
  private boolean newEnvironment;

  /**
   * Environment variables to use in the forked JVM.
   */
  private Environment env = new Environment();
  
  /**
   * Directory to invoke slave VM in.
   */
  private File dir;

  /**
   * Test names.
   */
  private final Resources resources;

  /**
   * Stop the build process if there were errors?
   */
  private boolean haltOnFailure = DEFAULT_HALT_ON_FAILURE;

  /**
   * Print summary of all tests at the end.
   */
  private boolean printSummary = DEFAULT_PRINT_SUMMARY;

  /**
   * Property to set if there were test failures or errors.
   */
  private String failureProperty;
  
  /**
   * A folder to store temporary files in. Defaults to {@link #dir} or  
   * the project's basedir.
   */
  private File tempDir;

  /**
   * Listeners listening on the event bus.
   */
  private List<Object> listeners = Lists.newArrayList();

  /**
   * Balancers scheduling tests for individual JVMs in parallel mode.
   */
  private List<TestBalancer> balancers = Lists.newArrayList();

  /**
   * Class loader used to resolve annotations and classes referenced from annotations
   * when {@link Description}s containing them are passed from slaves.
   */
  private AntClassLoader testsClassLoader;

  /**
   * @see #setParallelism(String)
   */
  private String parallelism = DEFAULT_PARALLELISM;

  /**
   * Set to true to leave temporary files (for diagnostics).
   */
  private boolean leaveTemporary;

  /**
   * A list of temporary files to leave or remove if build passes.
   */
  private List<File> temporaryFiles = Lists.newArrayList();

  /**
   * @see #setSeed(String)
   */
  private String random;

  /**
   * Multiple path resolution in {@link CommandlineJava#getCommandline()} is very slow
   * so we construct and canonicalize paths.
   */
  private Path classpath;
  private Path bootclasspath;

  /**
   * @see #setShuffleOnSlave(boolean)
   */
  private boolean shuffleOnSlave = DEFAULT_SHUFFLE_ON_SLAVE;

  /**
   * 
   */
  public JUnit4() {
    resources = new Resources();
    // resources.setCache(true);  // ANT 1.8.x+
  }
  
  /**
   * The number of parallel slaves. Can be set to a constant "max" for the
   * number of cores returned from {@link Runtime#availableProcessors()} or 
   * "auto" for sensible defaults depending on the number of cores.
   * The default is a single subprocess.
   * 
   * <p>Note that this setting forks physical JVM processes so it multiplies the 
   * requirements for heap memory, IO, etc. 
   */
  public void setParallelism(String parallelism) {
    this.parallelism = parallelism;
  }

  /**
   * Property to set to "true" if there is a failure in a test.
   */
  public void setFailureProperty(String failureProperty) {
    this.failureProperty = failureProperty;
  }
  
  /**
   * Initial random seed used for shuffling test suites and other sources
   * of pseudo-randomness. If not set, any random value is set. 
   * 
   * <p>The seed's format is compatible with {@link RandomizedRunner} so that
   * seed can be fixed for suites and methods alike.
   */
  public void setSeed(String randomSeed) {
    if (!Strings.isNullOrEmpty(getProject().getUserProperty(SYSPROP_RANDOM_SEED()))) {
      log("Ignoring seed attribute because it is overriden by user properties.", Project.MSG_WARN);
    } else if (!Strings.isNullOrEmpty(randomSeed)) {
      this.random = randomSeed;
    }
  }

  /**
   * Initializes custom prefix for all junit4 properties. This must be consistent
   * across all junit4 invocations if done from the same classpath. Use only when REALLY needed.
   */
  public void setPrefix(String prefix) {
    if (!Strings.isNullOrEmpty(getProject().getUserProperty(SYSPROP_PREFIX()))) {
      log("Ignoring prefix attribute because it is overriden by user properties.", Project.MSG_WARN);
    } else {
      SysGlobals.initializeWith(prefix);
    }
  }

  /**
   * @see #setSeed(String) 
   */
  public String getSeed() {
    return random;
  }  

  /**
   * Predictably shuffle tests order after balancing. This will help in spreading
   * lighter and heavier tests over a single slave's execution timeline while
   * still keeping the same tests order depending on the seed. 
   */ 
  public void setShuffleOnSlave(boolean shuffle) {
    this.shuffleOnSlave = shuffle;
  }

  /*
   * 
   */
  @Override
  public void setProject(Project project) {
    super.setProject(project);

    this.resources.setProject(project);
    this.classpath = new Path(getProject());
    this.bootclasspath = new Path(getProject());
  }
  
  /**
   * Prints the summary of all executed, ignored etc. tests at the end. 
   */
  public void setPrintSummary(boolean printSummary) {
    this.printSummary = printSummary;
  }

  /**
   * Stop the build process if there were failures or errors during test execution.
   */
  public void setHaltOnFailure(boolean haltOnFailure) {
    this.haltOnFailure = haltOnFailure;
  }

  /**
   * Set the maximum memory to be used by all forked JVMs.
   * 
   * @param max
   *          the value as defined by <tt>-mx</tt> or <tt>-Xmx</tt> in the java
   *          command line options.
   */
  public void setMaxmemory(String max) {
    if (!Strings.isNullOrEmpty(max)) {
      getCommandline().setMaxmemory(max);
    }
  }

  /**
   * Set to true to leave temporary files for diagnostics.
   */
  public void setLeaveTemporary(boolean leaveTemporary) {
    this.leaveTemporary = leaveTemporary;
  }
  
  /**
   * Add an additional argument to any forked JVM.
   */
  public Commandline.Argument createJvmarg() {
    return getCommandline().createVmArgument();
  }

  /**
   * The directory to invoke forked VMs in.
   */
  public void setDir(File dir) {
    this.dir = dir;
  }

  /**
   * The directory to store temporary files in.
   */
  public void setTempDir(File tempDir) {
    this.tempDir = tempDir;
  }

  /**
   * Adds a system property to any forked JVM.
   */
  public void addConfiguredSysproperty(Environment.Variable sysp) {
    getCommandline().addSysproperty(sysp);
  }
  
  /**
   * Adds a set of properties that will be used as system properties that tests
   * can access.
   * 
   * This might be useful to tranfer Ant properties to the testcases.
   */
  public void addSyspropertyset(PropertySet sysp) {
    getCommandline().addSyspropertyset(sysp);
  }
  
  /**
   * The command used to invoke the Java Virtual Machine, default is 'java'. The
   * command is resolved by java.lang.Runtime.exec().
   */
  public void setJvm(String jvm) {
    if (!Strings.isNullOrEmpty(jvm)) {
      getCommandline().setVm(jvm);
    }
  }

  /**
   * Adds an environment variable; used when forking.
   */
  public void addEnv(Environment.Variable var) {
    env.addVariable(var);
  }

  /**
   * Adds a set of tests based on pattern matching.
   */
  public void addFileSet(FileSet fs) {
    add(fs);
    if (fs.getProject() == null) {
      fs.setProject(getProject());
    }
  }

  /**
   * Adds a set of tests based on pattern matching.
   */
  public void add(ResourceCollection rc) {
    resources.add(rc);
  }  

  /**
   * Creates a new list of listeners.
   */
  public ListenersList createListeners() {
    return new ListenersList(listeners);
  }

  /**
   * Add assertions to tests execution.
   */
  public void addAssertions(Assertions asserts) {
    if (getCommandline().getAssertions() != null) {
        throw new BuildException("Only one assertion declaration is allowed");
    }
    getCommandline().setAssertions(asserts);
  }

  /**
   * Creates a new list of balancers.
   */
  public BalancersList createBalancers() {
    return new BalancersList(balancers);
  }

  /**
   * Adds path to classpath used for tests.
   * 
   * @return reference to the classpath in the embedded java command line
   */
  public Path createClasspath() {
    return classpath.createPath();
  }

  /**
   * Adds a path to the bootclasspath.
   * 
   * @return reference to the bootclasspath in the embedded java command line
   */
  public Path createBootclasspath() {
    return bootclasspath.createPath();
  }
  
  @Override
  public void execute() throws BuildException {
    validateJUnit4();

    // Initialize random if not already provided.
    if (random == null) {
      this.random = com.google.common.base.Objects.firstNonNull( 
          Strings.emptyToNull(getProject().getProperty(SYSPROP_RANDOM_SEED())),
          SeedUtils.formatSeed(new Random().nextLong()));
    }
    masterSeed();

    // Say hello and continue.
    log("<JUnit4> says hello. Random seed: " + getSeed(), Project.MSG_INFO);

    // Pass the random seed property.
    createJvmarg().setValue("-D" + SYSPROP_PREFIX() + "=" + CURRENT_PREFIX());
    createJvmarg().setValue("-D" + SYSPROP_RANDOM_SEED() + "=" + random);

    // Resolve paths first.
    this.classpath = resolveFiles(classpath);
    this.bootclasspath = resolveFiles(bootclasspath);
    getCommandline().createClasspath(getProject()).add(classpath);
    getCommandline().createBootclasspath(getProject()).add(bootclasspath);

    // Setup a class loader over test classes. This will be used for loading annotations
    // and referenced classes. This is kind of ugly, but mirroring annotation content will
    // be even worse and Description carries these.
    testsClassLoader = new AntClassLoader(
        this.getClass().getClassLoader(),
        getProject(),
        getCommandline().getClasspath(),
        true);

    // Pass method filter if any.
    String testMethodFilter = Strings.emptyToNull(getProject().getProperty(SYSPROP_TESTMETHOD()));
    if (testMethodFilter != null) {
      Environment.Variable v = new Environment.Variable();
      v.setKey(SYSPROP_TESTMETHOD());
      v.setValue(testMethodFilter);
      getCommandline().addSysproperty(v);
    }

    // Process test classes and resources.
    long start = System.currentTimeMillis();    
    final List<String> testClassNames = processTestResources();

    final EventBus aggregatedBus = new EventBus("aggregated");
    final TestsSummaryEventListener summaryListener = new TestsSummaryEventListener();
    aggregatedBus.register(summaryListener);
    
    for (Object o : listeners) {
      if (o instanceof ProjectComponent) {
        ((ProjectComponent) o).setProject(getProject());
      }
      if (o instanceof AggregatedEventListener) {
        ((AggregatedEventListener) o).setOuter(this);
      }
      aggregatedBus.register(o);
    }

    if (!testClassNames.isEmpty()) {
      start = System.currentTimeMillis();
      
      final int slaveCount = determineSlaveCount(testClassNames.size());
      final List<SlaveInfo> slaveInfos = Lists.newArrayList();
      for (int slave = 0; slave < slaveCount; slave++) {
        final SlaveInfo slaveInfo = new SlaveInfo(slave, slaveCount);
        slaveInfos.add(slaveInfo);
      }

      // Order test class names identically for balancers.
      Collections.sort(testClassNames);
      
      // Prepare a pool of suites dynamically dispatched to slaves as they become idle.
      final Deque<String> stealingQueue = 
          new ArrayDeque<String>(loadBalanceSuites(slaveInfos, testClassNames, balancers));
      aggregatedBus.register(new Object() {
        @Subscribe @SuppressWarnings("unused")
        public void onSlaveIdle(SlaveIdle slave) {
          if (stealingQueue.isEmpty()) {
            slave.finished();
          } else {
            slave.newSuite(stealingQueue.pop());
          }
        }
      });

      // Create callables for the executor.
      final List<Callable<Void>> slaves = Lists.newArrayList();
      for (int slave = 0; slave < slaveCount; slave++) {
        final SlaveInfo slaveInfo = slaveInfos.get(slave);
        slaves.add(new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            executeSlave(slaveInfo, aggregatedBus);
            return null;
          }
        });
      }

      ExecutorService executor = Executors.newCachedThreadPool();
      aggregatedBus.post(new AggregatedStartEvent(slaves.size(), testClassNames.size()));

      try {
        List<Future<Void>> all = executor.invokeAll(slaves);
        executor.shutdown();

        for (int i = 0; i < slaves.size(); i++) {
          Future<Void> f = all.get(i);
          try {
            f.get();
          } catch (ExecutionException e) {
            slaveInfos.get(i).executionError = e.getCause();
          }
        }
      } catch (InterruptedException e) {
        log("Master interrupted? Weird.", Project.MSG_ERR);
      }
      aggregatedBus.post(new AggregatedQuitEvent());

      for (SlaveInfo si : slaveInfos) {
        if (si.start > 0 && si.end > 0) {
          log(String.format(Locale.ENGLISH, "Slave %d: %8.2f .. %8.2f = %8.2fs",
              si.id,
              (si.start - start) / 1000.0f,
              (si.end - start) / 1000.0f,
              (si.getExecutionTime() / 1000.0f)), 
              Project.MSG_INFO);
        }
      }
      log(String.format(Locale.ENGLISH, "Execution time total: %.2fs", 
          (System.currentTimeMillis() - start) / 1000.0));

      SlaveInfo slaveInError = null;
      for (SlaveInfo i : slaveInfos) {
        if (i.executionError != null) {
          log("ERROR: Slave execution exception: " + 
              i.id + ", execution line: " + i.getCommandLine(), i.executionError, Project.MSG_ERR);
          if (slaveInError == null) {
            slaveInError = i;
          }
        }
      }

      if (slaveInError != null) {
        throw new BuildException("At least one slave process threw an unexpected exception, first: "
            + slaveInError.executionError.getMessage(), slaveInError.executionError);
      }
    }

    final TestsSummary testsSummary = summaryListener.getResult();
    if (printSummary) {
      log("Tests summary: " + testsSummary, Project.MSG_INFO);
    }

    if (!testsSummary.isSuccessful()) {
      if (!Strings.isNullOrEmpty(failureProperty)) {
        getProject().setNewProperty(failureProperty, "true");        
      }
      if (haltOnFailure) {
        throw new BuildException("There were test failures: " + testsSummary);
      }
    }

    if (!leaveTemporary) {
      for (File f : temporaryFiles) {
        f.delete();
      }
    }
  }

  /**
   * Validate JUnit4 presence in a concrete version.
   */
  private void validateJUnit4() throws BuildException {
    try {
      Class<?> clazz = Class.forName("org.junit.runner.Description");
      if (!Serializable.class.isAssignableFrom(clazz)) {
        throw new BuildException("At least JUnit version 4.10 is required on junit4's taskdef classpath.");
      }
    } catch (ClassNotFoundException e) {
      throw new BuildException("JUnit JAR must be added to junit4 taskdef's classpath.");
    }
  }

  /**
   * Perform load balancing of the set of suites. Sets {@link SlaveInfo#testSuites}
   * to suites preassigned to a given slave and returns a pool of suites
   * that should be load-balanced dynamically based on job stealing.
   */
  private List<String> loadBalanceSuites(List<SlaveInfo> slaveInfos,
      List<String> testClassNames, List<TestBalancer> balancers) {
    final List<TestBalancer> balancersWithFallback = Lists.newArrayList(balancers);
    balancersWithFallback.add(new RoundRobinBalancer());

    // Initialize per-slave lists.
    for (SlaveInfo si : slaveInfos) {
      si.testSuites = Lists.newArrayList();
    }

    // Go through all the balancers, the first one to assign a suite wins.
    final LinkedHashSet<String> remaining = Sets.newLinkedHashSet(testClassNames);
    for (TestBalancer balancer : balancersWithFallback) {
      balancer.setOwner(this);

      Map<String, Integer> assignments = balancer.assign(
          Collections.unmodifiableCollection(remaining), slaveInfos.size(), masterSeed());
      for (Map.Entry<String, Integer> e : assignments.entrySet()) {
        if (e.getValue() == null) {
          throw new RuntimeException("Balancer must return non-null slave ID.");
        }
        if (!remaining.remove(e.getKey())) {
          throw new RuntimeException("Balancer must return suite name: " + e.getKey());
        }
        log("Suite " + e.getKey() + " assigned to slave " + e.getValue() + "  by "
            + "balancer " + balancer.getClass().getSimpleName(), Project.MSG_VERBOSE);
        slaveInfos.get(e.getValue()).testSuites.add(e.getKey());
      }
    }

    if (remaining.size() != 0) {
      throw new RuntimeException("Not all suites assigned?: " + remaining);
    }    

    // Take a fraction of suites scheduled as last on each slave and move them to a common
    // job-stealing queue.
    List<String> stealingQueue = Lists.newArrayList();
    for (SlaveInfo si : slaveInfos) {
      ArrayList<String> slaveSuites = si.testSuites;
      int moveToCommon = slaveSuites.size() / 2; // 50%
      if (moveToCommon > 0) {
        List<String> sub = 
            slaveSuites.subList(slaveSuites.size() - moveToCommon, slaveSuites.size());
        stealingQueue.addAll(sub);
        sub.clear();
      }
    }

    // Shuffle the remaining jobs.
    if (shuffleOnSlave) {
      for (SlaveInfo si : slaveInfos) {
        Collections.sort(si.testSuites);
        Collections.shuffle(si.testSuites, new Random(this.masterSeed()));
      }
    }

    return stealingQueue;
  }

  /**
   * Return the master seed of {@link #getSeed()}.
   */
  private long masterSeed() {
    long[] seeds = SeedUtils.parseSeedChain(getSeed());
    if (seeds.length < 1) {
      throw new BuildException("Random seed is required.");
    }
    return seeds[0];
  }

  /**
   * Resolve all files from a given path and simplify its definition.
   */
  private Path resolveFiles(Path path) {
    Path cloned = new Path(getProject());
    for (String location : path.list()) {
      cloned.createPathElement().setLocation(new File(location));
    }
    return cloned;
  }

  /**
   * Determine how many slaves to use.
   */
  private int determineSlaveCount(int testCases) {
    int cores = Runtime.getRuntime().availableProcessors();
    int slaveCount;
    if (this.parallelism.equals(PARALLELISM_AUTO)) {
      if (cores >= 8) {
        // Maximum parallel jvms is 4, conserve some memory and memory bandwidth.
        slaveCount = 4;
      } else if (cores >= 4) {
        // Make some space for the aggregator.
        slaveCount = 3;
      } else {
        // even for dual cores it usually makes no sense to fork more than one
        // JVM.
        slaveCount = 1;
      }
    } else if (this.parallelism.equals(PARALLELISM_MAX)) {
      slaveCount = Runtime.getRuntime().availableProcessors();
    } else {
      try {
        slaveCount = Math.max(1, Integer.parseInt(parallelism));
      } catch (NumberFormatException e) {
        throw new BuildException("parallelism must be 'auto', 'max' or a valid integer: "
            + parallelism);
      }
    }

    slaveCount = Math.min(testCases, slaveCount);
    return slaveCount;
  }

  /**
   * Attach listeners and execute a slave process.
   */
  private void executeSlave(final SlaveInfo slave, final EventBus aggregatedBus)
    throws Exception
  {
    final File classNamesFile = File.createTempFile(
        "junit4-slave-" + slave.id, ".suites", getTempDir());
    temporaryFiles.add(classNamesFile);

    final File classNamesDynamic = new File(
        classNamesFile.getPath().replace(".suites", "-dynamic.suites"));

    // Dump all test class names to a temporary file.
    String testClassPerLine = Joiner.on("\n").join(slave.testSuites);
    log("Test class names:\n" + testClassPerLine, Project.MSG_VERBOSE);

    Files.write(testClassPerLine, classNamesFile, Charsets.UTF_8);

    // Prepare command line for java execution.
    CommandlineJava commandline;
    commandline = (CommandlineJava) getCommandline().clone();
    commandline.createClasspath(getProject()).add(addSlaveClasspath());
    commandline.setClassname(SlaveMainSafe.class.getName());
    if (slave.slaves == 1) {
      commandline.createArgument().setValue(SlaveMain.OPTION_FREQUENT_FLUSH);
    }
    commandline.createArgument().setValue("@" + classNamesFile.getAbsolutePath());

    // Emit command line before -stdin to avoid confusion.
    slave.slaveCommandLine = Joiner.on(" ").join(commandline.getCommandline());
    log("Slave process command line:\n" + 
        slave.slaveCommandLine, Project.MSG_VERBOSE);

    commandline.createArgument().setValue(SlaveMain.OPTION_STDIN);

    final EventBus eventBus = new EventBus("slave-" + slave.id);
    final DiagnosticsListener diagnosticsListener = new DiagnosticsListener(slave, getProject());
    eventBus.register(diagnosticsListener);
    eventBus.register(new AggregatingListener(aggregatedBus, slave));
    
    final PrintWriter w = new PrintWriter(Files.newWriter(classNamesDynamic, Charset.defaultCharset()));
    eventBus.register(new Object() {
      @SuppressWarnings("unused")
      @Subscribe
      public void onIdleSlave(final SlaveIdle slave) {
        aggregatedBus.post(new SlaveIdle() {
          @Override
          public void finished() {
            slave.finished();
          }

          @Override
          public void newSuite(String suiteName) {
            w.println(suiteName);
            slave.newSuite(suiteName);
          }
        });
      }
    });

    try {
      forkProcess(eventBus, commandline);
    } finally {
      Closeables.closeQuietly(w);
      Files.copy(classNamesDynamic,
          Files.newOutputStreamSupplier(classNamesFile, true));
      classNamesDynamic.delete();
    }

    if (!diagnosticsListener.quitReceived()) {
      throw new BuildException("Quit event not received from a slave process?");
    }
  }

  /**
   * Execute a slave process. Pump events to the given event bus.
   */
  private void forkProcess(EventBus eventBus, CommandlineJava commandline) {
    try {
      final LocalSlaveStreamHandler streamHandler = 
          new LocalSlaveStreamHandler(eventBus, testsClassLoader, System.err);
      final Execute execute = new Execute();
      execute.setCommandline(commandline.getCommandline());
      execute.setVMLauncher(true);
      execute.setWorkingDirectory(dir == null ? getProject().getBaseDir() : dir);
      execute.setStreamHandler(streamHandler);
      execute.setNewenvironment(newEnvironment);
      if (env.getVariables() != null)
        execute.setEnvironment(env.getVariables());
      getProject().log("Starting slave.", Project.MSG_DEBUG);
      int exitStatus = execute.execute();
      getProject().log("Slave finished with exit code: " + exitStatus, Project.MSG_DEBUG);

      if (streamHandler.isErrorStreamNonEmpty()) {
        log(">>> error stream from forked JVM (verbatim) ----", Project.MSG_ERR);
        log(streamHandler.getErrorStreamAsString(), Project.MSG_ERR);
        log("<<< EOF ----", Project.MSG_ERR);

        // Anything on the altErr will cause a build failure.
        String msg = "Unexpected output from forked JVM. This" +
            " most likely indicates JVM crash. Inspect the logs above and look for crash" +
            " dumps in: " + getProject().getBaseDir().getAbsolutePath();
        log(msg, Project.MSG_ERR);
        throw new BuildException("Unexpected output from forked JVM. This" +
            " most likely indicates JVM crash.");
      }

      if (execute.isFailure()) {
        if (exitStatus == SlaveMain.ERR_NO_JUNIT) {
          throw new BuildException("Forked JVM's classpath must include a junit4 JAR.");
        }
        if (exitStatus == SlaveMain.ERR_OLD_JUNIT) {
          throw new BuildException("Forked JVM's classpath must use JUnit 4.10 or newer.");
        }
        throw new BuildException("Forked process exited with an error code: " + exitStatus);
      }
    } catch (IOException e) {
      throw new BuildException("Could not execute slave process. Run ant with -verbose to get" +
      		" the execution details.", e);
    }
  }

  /**
   * Resolve temporary folder.
   */
  private File getTempDir() {
    if (this.tempDir == null) {
      if (this.dir != null) {
        this.tempDir = dir;
      } else {
        this.tempDir = getProject().getBaseDir();
      }
    }
    return tempDir;
  }

  /**
   * Process test resources. If there are any test resources that are _not_ class files,
   * this will cause a build error.   
   */
  private List<String> processTestResources() {
    List<String> testClassNames = Lists.newArrayList();
    resources.setProject(getProject());
    
    @SuppressWarnings("unchecked")
    Iterator<Resource> iter = (Iterator<Resource>) resources.iterator();
    while (iter.hasNext()) {
      final Resource r = iter.next();
      if (!r.isExists()) 
        throw new BuildException("Test class resource does not exist?: " + r.getName());

      try {
        InputStream is = r.getInputStream();
        if (!is.markSupported()) {
          is = new BufferedInputStream(is);          
        }

        try {
          is.mark(4);
          if (is.read() != 0xca ||
              is.read() != 0xfe ||
              is.read() != 0xba ||
              is.read() != 0xbe) {
            throw new BuildException("File does not start with a class magic 0xcafebabe: "
                + r.getName() + ", " + r.getLocation());
          }
          is.reset();

          ClassReader reader = new ClassReader(is);
          String className = reader.getClassName().replace('/', '.');
          getProject().log("Test class parsed: " + r.getName() + " as " 
              + reader.getClassName(), Project.MSG_DEBUG);
          testClassNames.add(className);
        } finally {
          is.close();
        }
      } catch (IOException e) {
        throw new BuildException("Could not read or parse as Java class: "
            + r.getName() + ", " + r.getLocation());
      }
    }

    String testClassFilter = Strings.emptyToNull(getProject().getProperty(SYSPROP_TESTCLASS()));
    if (testClassFilter != null) {
      ClassGlobFilter filter = new ClassGlobFilter(testClassFilter);
      for (Iterator<String> i = testClassNames.iterator(); i.hasNext();) {
        if (!filter.shouldRun(Description.createSuiteDescription(i.next()))) {
          i.remove();
        }
      }
    }

    return testClassNames;
  }

  /**
   * Returns the slave VM command line.
   */
  private CommandlineJava getCommandline() {
    return slaveCommand;
  }

  /**
   * Adds a classpath source which contains the given resource.
   */
  private Path addSlaveClasspath() {
    Path path = new Path(getProject());

    String [] REQUIRED_SLAVE_CLASSES = {
        SlaveMain.class.getName(),
        Strings.class.getName(),
        MethodGlobFilter.class.getName(),
        Gson.class.getName()
    };

    for (String clazz : Arrays.asList(REQUIRED_SLAVE_CLASSES)) {
      String resource = clazz.replace(".", "/") + ".class";
      File f = LoaderUtils.getResourceSource(getClass().getClassLoader(), resource);
      if (f != null) {
        path.createPath().setLocation(f);
      } else {
        throw new BuildException("Could not locate classpath for resource: "
            + resource);
      }
    }
    return path;
  }
}
