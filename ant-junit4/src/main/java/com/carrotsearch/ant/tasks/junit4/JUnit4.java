package com.carrotsearch.ant.tasks.junit4;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
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
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

/**
 * A simple ANT task to run JUnit4 tests.
 */
public class JUnit4 extends Task {
  /**
   * Slave vm command line.
   */
  private CommandlineJava slaveCommand = new CommandlineJava();
  
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

  @Override
  public void execute() throws BuildException {
    getProject().log("<JUnit4> says hello.", Project.MSG_DEBUG);

    final List<String> testClasses = processTestResources();

    CommandlineJava commandline = getCommandline();
    commandline.createClasspath(getProject()).add(addSlaveClasspath());

    getProject().log("> " + Arrays.toString(commandline.getCommandline()), Project.MSG_INFO);
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
