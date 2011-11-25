package com.carrotsearch.ant.tasks.junit4;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PropertySet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.Resources;
import org.apache.tools.ant.util.LoaderUtils;
import org.junit.runner.Description;
import org.objectweb.asm.ClassReader;

import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatingListener;
import com.carrotsearch.ant.tasks.junit4.slave.SlaveMain;
import com.carrotsearch.ant.tasks.junit4.slave.SlaveMainSafe;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;

/**
 * A simple ANT task to run JUnit4 tests.
 */
public class JUnit4 extends Task {
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
  private boolean haltOnFailure;

  /**
   * Print summary of all tests at the end.
   */
  private boolean printSummary = true;

  /**
   * Property to set if there were test failures or errors.
   */
  private String failureProperty;
  
  /**
   * A folder to store temporary files in. Defaults to the project's basedir.
   */
  private File tempDir;
  
  /**
   * Listeners listening on the event bus.
   */
  private List<Object> listeners = Lists.newArrayList();

  /**
   * Class loader used to resolve annotations and classes referenced from annotations
   * when {@link Description}s containing them are passed from slaves.
   */
  private AntClassLoader testsClassLoader;

  /**
   * 
   */
  public JUnit4() {
    resources = new Resources();
    resources.setCache(true);
  }
  
  /**
   * Property to set to "true" if there is a failure in a test.
   */
  public void setFailureProperty(String failureProperty) {
    this.failureProperty = failureProperty;
  }
  
  /*
   * 
   */
  @Override
  public void setProject(Project project) {
    super.setProject(project);
    this.resources.setProject(project);
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
    getCommandline().setMaxmemory(max);
  }
  
  /**
   * Adds a JVM argument; ignored if not forking.
   * 
   * @return create a new JVM argument so that any argument can be passed to the
   *         JVM.
   */
  public Commandline.Argument createJvmarg() {
    return getCommandline().createVmArgument();
  }

  /**
   * Creates a new list of listeners.
   */
  public ListenersList createListeners() {
    return new ListenersList(listeners);
  }
  
  /**
   * Adds a system property to the forked JVM.
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
   * 
   * @param value
   *          the new VM to use instead of <tt>java</tt>
   */
  public void setJvm(String value) {
    getCommandline().setVm(value);
  }
  
  /**
   * Adds path to classpath used for tests.
   * 
   * @return reference to the classpath in the embedded java command line
   */
  public Path createClasspath() {
    return getCommandline().createClasspath(getProject()).createPath();
  }

  /**
   * Adds a path to the bootclasspath.
   * 
   * @return reference to the bootclasspath in the embedded java command line
   */
  public Path createBootclasspath() {
    return getCommandline().createBootclasspath(getProject()).createPath();
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
   * The directory to invoke the VM in.
   * 
   * @param dir
   *          the directory to invoke the JVM from.
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

  @Override
  public void execute() throws BuildException {
    getProject().log("<JUnit4> says hello.", Project.MSG_DEBUG);

    // Setup a class loader over test classes. This will be used for loading annotations
    // and referenced classes. This is kind of ugly, but mirroring annotation content will
    // be even worse and Description carries these.
    testsClassLoader = new AntClassLoader(
        this.getClass().getClassLoader(),
        getProject(),
        getCommandline().getClasspath(),
        true);

    // Process test classes and resources.
    final List<String> testClassNames = processTestResources();

    final EventBus aggregatedBus = new EventBus("aggregated");
    final TestsSummaryEventListener summaryListener = new TestsSummaryEventListener();
    aggregatedBus.register(summaryListener);
    
    for (Object o : listeners) {
      aggregatedBus.register(o);
    }

    // TODO: add class split and multiple slave execution.
    final int slaves = 1;
    final int slave = 0;
    final SlaveID slaveId = new SlaveID(slave, slaves);

    if (!testClassNames.isEmpty()) {
      try {
        executeSlave(slaveId, aggregatedBus, testClassNames);
      } catch (Throwable t) {
        if (t instanceof BuildException)
          throw (BuildException) t;
        else
          throw new BuildException(t);
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
  }

  /**
   * Attach listeners and execute a slave process.
   */
  private void executeSlave(SlaveID slave, EventBus aggregatedBus, List<String> testClassNames)
    throws IOException, BuildException
  {
    // Dump all test class names to a temporary file.
    File tempDir = getTempDir();
    String testClassPerLine = Joiner.on("\n").join(testClassNames);
    log("Test class names:\n" + testClassPerLine, Project.MSG_VERBOSE);

    final File classNamesFile = File.createTempFile("junit4-", ".testmethods", tempDir);
    Files.write(testClassPerLine, classNamesFile, Charsets.UTF_8);

    // Prepare command line for java execution.
    final CommandlineJava commandline = getCommandline();
    commandline.createClasspath(getProject()).add(addSlaveClasspath());
    commandline.setClassname(SlaveMainSafe.class.getName());
    commandline.createArgument().setValue("@" + classNamesFile.getAbsolutePath());
    log("Slave process command line:\n" + 
        Joiner.on(" ").join(commandline.getCommandline()), Project.MSG_VERBOSE);

    final EventBus eventBus = new EventBus("slave");
    final DiagnosticsListener diagnosticsListener = new DiagnosticsListener(slave, getProject());
    eventBus.register(diagnosticsListener);
    eventBus.register(new AggregatingListener(aggregatedBus, slave));
    executeProcess(eventBus, commandline);
    if (!diagnosticsListener.quitReceived()) {
      throw new BuildException("Quit event not received from a slave process?");
    }

    classNamesFile.delete();
  }

  /**
   * Execute a slave process. Pump events to the given event bus.
   */
  private void executeProcess(EventBus eventBus, CommandlineJava commandline) {
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
        log("-- error stream from forked JVM (verbatim) --", Project.MSG_ERR);
        log(streamHandler.getErrorStreamAsString(), Project.MSG_ERR);
        log("-- EOF --", Project.MSG_ERR);

        // Anything on the altErr will cause a build failure.
        log("An output appeared on the forked JVM's error stream. This is unexpected and" +
        		" most likely indicates JVM crash. Inspect the output above and possibly crash" +
            " dumps in the slave's current working directory.", Project.MSG_ERR);
        throw new BuildException("An output appeared on alternate error stream from slave process." +
        		" Check the logs. Subprocess exit code: " + exitStatus);
      }

      if (execute.isFailure()) {
        if (exitStatus == SlaveMain.ERR_NO_JUNIT) {
          throw new BuildException("Forked JVM's classpath must include a junit4 JAR.");
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
      this.tempDir = getProject().getBaseDir();
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
        try {
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
