package com.carrotsearch.ant.tasks.junit4;

import java.io.*;
import java.util.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.types.resources.Resources;
import org.apache.tools.ant.util.LineOrientedOutputStream;
import org.apache.tools.ant.util.LoaderUtils;
import org.objectweb.asm.ClassReader;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * A simple ANT task to run JUnit4 tests.
 */
public class JUnit4 extends Task {
  /**
   * Listeners receiving test output progress. 
   */
  private List<IExecutionListener> listeners = Lists.newArrayList();

  /**
   * Slave vm command line.
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
   * A folder to store temporary files in. Defaults to the project's basedir.
   */
  private File tempDir;
  
  public JUnit4() {
    resources = new Resources();
    resources.setCache(true);
  }
  
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

    // Process test classes and resources.
    final List<String> testClassNames = processTestResources();
    if (!testClassNames.isEmpty()) {
      executeSlave(testClassNames);
    }
  }

  /**
   * Attach listeners and execute a slave process.
   */
  private void executeSlave(List<String> testClassNames) {
    // Dump all test class names to a temporary file.
    File tempDir = getTempDir();
    File classNamesFile = null;
    try {
      String testClassPerLine = Joiner.on("\n").join(testClassNames);
      log("Test class names:\n" + testClassPerLine, Project.MSG_VERBOSE);

      classNamesFile = File.createTempFile("junit4-", ".testmethods", tempDir);
      Files.write(testClassPerLine, classNamesFile, Charsets.UTF_8);

      // Prepare command line for java execution.
      final CommandlineJava commandline = getCommandline();
      commandline.createClasspath(getProject()).add(addSlaveClasspath());
      commandline.setClassname(SlaveMainSafe.class.getName());
      commandline.createArgument().setValue("@" + classNamesFile.getAbsolutePath());
      log("Slave process command line:\n" + 
          Joiner.on("\n").join(commandline.getCommandline()), Project.MSG_VERBOSE);

      TestsSummaryListener summaryListener = new TestsSummaryListener();
      listeners.add(summaryListener);
      executeProcess(commandline);

      final TestsSummary testsSummary = summaryListener.getResult();
      if (printSummary) {
        log("Tests summary: " + testsSummary, Project.MSG_INFO);
      }
      if (haltOnFailure && !testsSummary.isSuccessful()) {
        throw new BuildException("There were test failures: " + testsSummary);
      }
    } catch (IOException e) {
      throw new BuildException(e);
    } finally {
      if (testClassNames != null) {
        classNamesFile.delete();
      }
    }
  }

  /**
   * Stream handler for the subprocess.
   */
  private class SlaveStreamHandler implements ExecuteStreamHandler {
    private List<Thread> pumpers = Lists.newArrayList();
    private IExecutionListener listener;

    public SlaveStreamHandler(IExecutionListener listener) {
      this.listener = listener;
    }

    @Override
    public void setProcessOutputStream(final InputStream is) throws IOException {
      Thread t = new Thread("pumper-stdout") {
        public void run() {
          EventReader replay = new EventReader(is, listener, IExecutionListener.class);
          try {
            replay.replay();
          } catch (IOException e) {
            log("PANIC: Event replay exception: " + e.getMessage(), e, Project.MSG_ERR);
            readUntilEOF(is);
          }
        }

        private void readUntilEOF(InputStream is) {
          byte [] buffer = new byte [1024 * 8];
          try {
            while (is.read(buffer) > 0);
          } catch (IOException e) {
            // Ignore and exit.
          }
        }
      };
      pumpers.add(t);
    }

    @Override
    public void setProcessInputStream(OutputStream os) throws IOException {
      // do nothing.
    }
    
    @Override
    public void setProcessErrorStream(InputStream is) throws IOException {
      OutputStream os = new LineOrientedOutputStream() {
        protected void processLine(String line) throws IOException {
          System.err.println("!> " + line);
        }
      };
      StreamPumper streamPumper = new StreamPumper(is, os, false);
      pumpers.add(new Thread(streamPumper, "pumper-stderr"));
    }

    @Override
    public void start() throws IOException {
      for (Thread t : pumpers) {
        t.setDaemon(true);
        t.start();
      }
    }

    @Override
    public void stop() {
      try {
        for (Thread t : pumpers) {
            log("Stopping: " + t, Project.MSG_DEBUG);
            t.join();
        }
      } catch (InterruptedException e) {
        // Don't wait.
      }
    }
  }

  /**
   * Execute a slave process.
   */
  private void executeProcess(CommandlineJava commandline) {
    try {
      final IExecutionListener listener = SlaveMain.listenerProxy(
          Multiplexer.forInterface(IExecutionListener.class, listeners));
      final ExecuteStreamHandler streamHandler = new SlaveStreamHandler(listener);
      final Execute execute = new Execute();
      execute.setCommandline(commandline.getCommandline());
      execute.setVMLauncher(true);
      execute.setWorkingDirectory(dir == null ? getProject().getBaseDir() : dir);
      execute.setStreamHandler(streamHandler);
      execute.setNewenvironment(newEnvironment);
      if (env.getVariables() != null)
        execute.setEnvironment(env.getVariables());
      int exitStatus = execute.execute();

      if (execute.isFailure()) {
        if (exitStatus == SlaveMain.ERR_NO_JUNIT) {
          throw new BuildException("Slave process classpath must include a junit JAR.");
        }
        throw new BuildException("Slave process exited with an error code: " + exitStatus);
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
