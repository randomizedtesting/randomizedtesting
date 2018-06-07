package com.carrotsearch.maven.plugins.junit4;

import static com.google.common.base.MoreObjects.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.antrun.AntrunXmlPlexusConfigurationWriter;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.artifact.filter.PatternIncludesArtifactFilter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.carrotsearch.ant.tasks.junit4.JUnit4;
import com.carrotsearch.ant.tasks.junit4.listeners.TextReport;
import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.google.common.base.Strings;
import com.google.common.io.Closer;

/**
 * Run tests using a delegation to <a
 * href="https://github.com/carrotsearch/randomizedtesting/">Randomized
 * Testing's JUnit4</a> ANT task.
 */
@Mojo(
    name = "junit4",
    defaultPhase = LifecyclePhase.TEST,
    threadSafe = true,
    requiresProject = true,
    requiresDependencyResolution = ResolutionScope.TEST)
public class JUnit4Mojo extends AbstractMojo {
  /** An empty String[]. */
  private static final String[] EMPTY_STRING_ARRAY = new String [] {};

  /** An empty Map<String, String>. */
  private static final Map<String,String> EMPTY_STRING_STRING_MAP = Collections.emptyMap();

  /** Default target in the generated ANT file. */
  private static final String DEFAULT_TARGET = "__default__";

  /**
   * The Maven project object
   */
  @Parameter(
      property = "project", 
      readonly = true, 
      required = true)
  private MavenProject project;

  /**
   * Base directory to invoke slave VMs in. Also note <code>isolateWorkingDirectories</code>
   * parameter.
   */
  @Parameter(
      property = "project.build.directory",
      readonly = true, 
      required = true)
  private File dir;

  /**
   * The directory to store temporary files in.
   */
  @Parameter(
      property = "project.build.directory")
  private File tempDir;

  /**
   * The number of parallel f. Can be set to a constant "max" for the
   * number of cores returned from {@link Runtime#availableProcessors()} or 
   * "auto" for sensible defaults depending on the number of cores.
   * The default is a single subprocess.
   * 
   * <p>Note that this setting forks physical JVM processes so it multiplies the 
   * requirements for heap memory, IO, etc.
   */
  @Parameter(defaultValue = "1")
  private String parallelism = JUnit4.DEFAULT_PARALLELISM;

  /**
   * Property to set to "true" if there is a failure in a test. The use of this
   * property is discouraged in Maven (builds should be declarative).
   */
  @Parameter
  private String failureProperty;

  /**
   * Initial random seed used for shuffling test suites and other sources
   * of pseudo-randomness. If not set, any random value is set. 
   * 
   * <p>The seed's format is compatible with {@link RandomizedRunner} so that
   * seed can be fixed for suites and methods alike. Unless the global prefix of
   * randomized testing properties is changed, the seed can be overridden using "tests.seed"
   * property.
   */
  @Parameter(property = "tests.seed", defaultValue = "")
  private String seed;

  /**
   * Predictably shuffle tests order after balancing. This will help in spreading
   * lighter and heavier tests over a single slave's execution timeline while
   * still keeping the same tests order depending on the seed.
   */
  @Parameter(defaultValue = "true")
  private boolean shuffleOnSlave = JUnit4.DEFAULT_SHUFFLE_ON_SLAVE;
  
  /**
   * Prints the summary of all executed, ignored etc. tests at the end.
   */
  @Parameter(defaultValue = "true")
  private boolean printSummary = JUnit4.DEFAULT_PRINT_SUMMARY;

  /**
   * Stop the build process if there were failures or errors during test execution.
   */
  @Parameter(defaultValue = "true")
  private boolean haltOnFailure = JUnit4.DEFAULT_HALT_ON_FAILURE;

  /**
   * If set to <code>true</code> each forked JVM gets a separate working directory
   * under whatever is set in <code>dir</code>. The directory naming for each JVM
   * follows: "J<i>num</i>", where <i>num</i> is the forked JVM's number. 
   * Directories are created automatically and removed unless <code>leaveTemporary</code> 
   * is set to <code>true</code>.
   */
  @Parameter(defaultValue = "true")
  private boolean isolateWorkingDirectories = JUnit4.DEFAULT_ISOLATE_WORKING_DIRECTORIES;

  /**
   * Sets the action performed when current work directory for a forked JVM is not empty
   * and <code>isolateWorkingDirectories</code> is set to true.
   */
  @Parameter(defaultValue = "fail")
  private String onNonEmptyWorkDirectory = JUnit4.DEFAULT_NON_EMPTY_WORKDIR_ACTION.name();

  /**
   * If set to true, any sysout and syserr calls will be written to original
   * output and error streams (and in effect will appear as "jvm output". By default
   * sysout and syserrs are captured and proxied to the event stream to be synchronized
   * with other test events but occasionally one may want to synchronize them with direct 
   * JVM output (to synchronize with compiler output or GC output for example).
   */
  @Parameter(defaultValue = "false")
  private boolean sysouts = JUnit4.DEFAULT_SYSOUTS;
  
  /**
   * Specifies the ratio of suites moved to dynamic assignment list. A dynamic
   * assignment list dispatches suites to the first idle slave JVM. Theoretically
   * this is an optimal strategy, but it is usually better to have some static assignments
   * to avoid communication costs.
   * 
   * <p>A ratio of 0 means only static assignments are used. A ratio of 1 means
   * only dynamic assignments are used.
   * 
   * <p>The list of dynamic assignments is sorted by decreasing cost (always) and
   * is inherently prone to race conditions in distributing suites. Should there
   * be an error based on suite-dependency it will not be directly repeatable. In such
   * case use the per-slave-jvm list of suites file dumped to disk for each slave JVM.
   * (see <code>leaveTemporary</code> parameter).
   */
  @Parameter(defaultValue = "0.25")
  private float dynamicAssignmentRatio = JUnit4.DEFAULT_DYNAMIC_ASSIGNMENT_RATIO;
  
  /**
   * Set the maximum memory to be used by all forked JVMs. The value as 
   * defined by <tt>-mx</tt> or <tt>-Xmx</tt> in the java
   * command line options.
   */
  @Parameter
  private String maxMemory;

  /**
   * Set to true to leave temporary files for diagnostics.
   */
  @Parameter
  private boolean leaveTemporary;

  /**
   * Add an additional argument to any forked JVM.
   */
  @Parameter
  private String [] jvmArgs;

  /**
   * Arbitrary JVM options to set on the command line.
   */
  @Parameter(property = "argLine")
  private String argLine;

  /**
   * Adds a system property to any forked JVM.
   */
  @Parameter
  private Map<String, String> systemProperties; 

  /**
   * Adds an environment variable to any forked JVM.
   */
  @Parameter
  private Map<String, String> environmentVariables; 

  /**
   * The command used to invoke the Java Virtual Machine, default is 'java'. The
   * command is resolved by java.lang.Runtime.exec().
   */
  @Parameter(defaultValue = "java")
  private String jvm;

  /**
   * The directory containing generated test classes of the project being
   * tested. This will be included at the beginning of the test classpath.
   */
  @Parameter(defaultValue = "${project.build.testOutputDirectory}")
  private File testClassesDirectory;

  /**
   * The directory containing generated classes of the project being
   * tested. This will be included after <code>testClassesDirectory</code>.
   */
  @Parameter(defaultValue = "${project.build.outputDirectory}")
  private File classesDirectory;

  /**
   * A list of &lt;include&gt; elements specifying the tests (by pattern) that should be included in testing. When not
   * specified defaults to: 
   * <pre>
   * &lt;include&gt;**&#47;*Test.class&lt;/include&gt;
   * &lt;include&gt;**&#47;Test*.class&lt;/include&gt;
   * </pre>
   * Note that this may result in nested classes being included for tests. Use proper exclusion patterns. 
   */
  @Parameter
  private List<String> includes;

  /**
   * A list of &lt;exclude&gt; elements specifying the tests (by pattern) that should be excluded in testing. When not
   * specified defaults to: 
   * <pre>
   * &lt;exclude&gt;**&#47;*$*.class&lt;/exclude&gt;
   * </pre>
   * This patterns excludes any nested classes that might otherwise be included. 
   */
  @Parameter
  private List<String> excludes;

  /**
   * This parameter adds a listener emitting surefire-compatible XMLs if no other listeners
   * are added. If there are any configured listeners, this parameter is omitted (you can
   * add a maven-compatible listener manually).
   */
  @Parameter(defaultValue = "${project.build.directory}/surefire-reports")
  private File surefireReportsDirectory;

  /**
   * Specifies the name of the JUnit artifact used for running tests. JUnit dependency
   * must be in at least version 4.10.
   */
  @Parameter(
      property = "junitArtifactName", 
      defaultValue = "junit:junit")
  private String junitArtifactName;

  /**
   * What should be done on unexpected JVM output? JVM may write directly to the 
   * original descriptors, bypassing redirections of System.out and System.err. Typically,
   * these messages will be important and should fail the build (permgen space exceeded,
   * compiler errors, crash dumps). However, certain legitimate logs (gc activity, class loading
   * logs) are also printed to these streams so sometimes the output can be ignored.
   * 
   * <p>Allowed values (any comma-delimited combination of): ignore, pipe, warn, fail.
   */
  @Parameter(
      property = "jvmOutputAction", 
      defaultValue = "pipe,warn")
  private String jvmOutputAction;

  /**
   * Allows or disallow duplicate suite names in resource collections. By default this option
   * is <code>true</code> because certain ANT-compatible report types (like XML reports)
   * will have a problem with duplicate suite names (will overwrite files).
   */
  @Parameter(defaultValue = "" + JUnit4.DEFAULT_UNIQUE_SUITE_NAME)
  private boolean uniqueSuiteNames = JUnit4.DEFAULT_UNIQUE_SUITE_NAME;
  
  /**
   * Raw listeners configuration. Same XML as for ANT.
   */
  @Parameter
  private PlexusConfiguration listeners;

  /**
   * Raw runListeners configuration. Same XML as for ANT.
   */
  @Parameter
  private PlexusConfiguration runListeners;


  /**
   * Raw assertions configuration. Same XML as for ANT.
   */
  @Parameter
  private PlexusConfiguration assertions;

  /**
   * Raw balancers configuration. Same XML as for ANT.
   */
  @Parameter
  private PlexusConfiguration balancers;

  /**
   * Raw section to copy/paste into ANT-driver.
   */
  @Parameter
  private PlexusConfiguration verbatim;

  /**
   * Sets the heartbeat used to detect inactive/ hung forked tests (JVMs) to the given
   * number of seconds. The heartbeat detects
   * no-event intervals and will report them to listeners. Notably, {@link TextReport} report will
   * emit heartbeat information (to a file or console).
   * 
   * <p>Setting the heartbeat to zero means no detection.
   */
  @Parameter(defaultValue = "0")
  private long heartbeat;

  /**
   * Set this to "true" to skip running tests, but still compile them. Its use
   * is NOT RECOMMENDED, but quite convenient on occasion.
   */
  @Parameter(
      property = "skipTests",
      defaultValue = "false")
  private boolean skipTests;

  /**
   * Project packaging mode to skip POM-projects
   */
  @Parameter(
      defaultValue = "${project.packaging}",
      readonly = true)
  private String packaging;

  /**
   * List of dependencies to exclude from the test classpath. Each dependency 
   * string must follow the format
   * <i>groupId:artifactId</i>. For example: <i>org.acme:project-a</i>
   * 
   * <p>This is modeled after surefire. An excluded dependency does <b>not</b> mean its
   * transitive dependencies will also be excluded. 
   */
  @Parameter
  private List<String> classpathDependencyExcludes;

  /**
   * A dependency scope to exclude from the test classpath. The scope can be one of the following scopes:
   * <p/>
   * <ul>
   * <li><i>compile</i> - system, provided, compile
   * <li><i>runtime</i> - compile, runtime
   * <li><i>test</i> - system, provided, compile, runtime, test
   * </ul>
   */
  @Parameter(defaultValue = "")
  private String classpathDependencyScopeExclude;

  /**
   * Additional elements to be appended to the classpath.
   */
  @Parameter
  private List<String> additionalClasspathElements; 

  /**
   * Initializes custom prefix for all junit4 properties. This must be consistent
   * across all junit4 invocations if done from the same classpath. Use only when REALLY needed. 
   */
  @Parameter()
  private String prefix;

  /**
   * Enables a debug stream from each forked JVM. This will create an additional file
   * next to each events file. For debugging the framework only, not a general-purpose setting.  
   */
  @Parameter()
  private Boolean debugStream;

  /**
   * Set new environment for the forked process?
   */
  @Parameter()
  private Boolean newEnvironment;

  /**
   * What to do when no tests were executed (all tests were ignored)? Possible values:
   * ignore, fail, warn.
   */
  @Parameter(defaultValue = "ignore")
  private String ifNoTests;

  /**
   * Sets the property prefix to which test statistics are saved.
   */  
  @Parameter()
  private String statsPropertyPrefix;
  
  /**
   * Map of plugin artifacts.
   */
  @Parameter(
      defaultValue = "${plugin.artifactMap}",
      required = true,
      readonly = true)
  private Map<String,Artifact> pluginArtifactMap;

  /**
   * Map of project artifacts.
   */
  @Parameter(
      defaultValue = "${project.artifactMap}",
      required = true,
      readonly = true)
  private Map<String,Artifact> projectArtifactMap;

  /**
   * The current build session instance.
   */
  @Parameter(
      property = "session",
      required = true,
      readonly = true)
  private MavenSession session;

  /**
   * Repository.
   * 
   * @component
   * @readonly
   * @required
   */
  @Component
  private RepositorySystem repositorySystem; 

  /**
   * For retrieval of artifact's metadata.
   */
  @SuppressWarnings("deprecation")
  @Component
  private org.apache.maven.artifact.metadata.ArtifactMetadataSource metadataSource;

  /**
   * 
   */
  @Component
  private ArtifactResolver resolver;
  
  
  /**
   * Run the mojo.
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if ("pom".equals(packaging)) {
      getLog().debug("Skipping execution for packaging \"" + packaging + "\"");
      return;
    }

    // Check directory existence first.
    if (!dir.isDirectory() ||
        !tempDir.isDirectory()) {
      getLog().warn("Location does not exist or is not a directory: " + dir.getAbsolutePath());
      skipTests = true;
    }

    if (skipTests) {
      return;
    }

    validateParameters();

    // Ant project setup.
    final Project antProject = new Project();
    antProject.init();
    antProject.setBaseDir(dir);
    antProject.addBuildListener(new MavenListenerAdapter(getLog()));

    // Generate JUnit4 ANT task model and generate a synthetic ANT file.
    Document doc = DocumentFactory.getInstance().createDocument();
    try {
      populateJUnitElement(createDocumentSkeleton(doc));
      File tempAntFile = createTemporaryAntFile(doc);

      ProjectHelper.configureProject(antProject, tempAntFile);
      try {
        antProject.executeTarget(DEFAULT_TARGET);
      } finally {
        if (!leaveTemporary) {
          tempAntFile.delete();
        }
      }
    } catch (IOException e) {
      throw new MojoExecutionException("An I/O error occurred: " + e.getMessage(), e);
    } catch (BuildException e) {
      throw new MojoExecutionException(e.getMessage(), e.getCause());
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Initial validation of input parameters and configuration.
   */
  private void validateParameters() throws MojoExecutionException {
    // Check for junit dependency on project level.
    Artifact junitArtifact = projectArtifactMap.get(junitArtifactName);

    if (junitArtifact == null) {
      throw new MojoExecutionException("Missing JUnit artifact in project dependencies: "
          + junitArtifactName);
    }
    checkVersion("JUnit", "[4.10,)", junitArtifact);
    
    // Fill in complex defaults if not given.
    if (includes == null || includes.isEmpty()) {
      includes = Arrays.asList(
          "**/Test*.class", "**/*Test.class");
    }
    if (excludes == null || excludes.isEmpty()) {
      excludes  = Arrays.asList(
          "**/*$*.class");
    }
  }

  /**
   * Ensure <code>artifactName</code> matches version <code>versionSpec</code>.
   */
  private void checkVersion(String artifactName, String versionSpec, Artifact artifact) 
      throws MojoExecutionException {
    VersionRange range;
    try {
      range = VersionRange.createFromVersionSpec(versionSpec);
      if (!range.containsVersion(new DefaultArtifactVersion(artifact.getVersion())))
      {
          throw new MojoExecutionException(
              "JUnit4 plugin requires " + artifactName + " in version " + 
              versionSpec + " among project dependencies, you declared: "
                  + artifact.getVersion());
      }
    } catch (InvalidVersionSpecificationException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Populate junit4 task with attributes and values.
   */
  private void populateJUnitElement(Element junit4) {
    // Attributes.
    if (dir != null) junit4.addAttribute("dir", dir.getAbsolutePath());
    if (tempDir != null) junit4.addAttribute("tempDir", tempDir.getAbsolutePath());
    if (parallelism != null) junit4.addAttribute("parallelism", parallelism);
    if (failureProperty != null) junit4.addAttribute("failureProperty", failureProperty);
    if (seed != null) junit4.addAttribute("seed", seed);
    if (jvm != null) junit4.addAttribute("jvm", jvm);
    if (maxMemory != null) junit4.addAttribute("maxMemory", maxMemory);
    if (jvmOutputAction != null) junit4.addAttribute("jvmOutputAction", jvmOutputAction);
    if (heartbeat != 0) junit4.addAttribute("heartbeat", Long.toString(heartbeat));
    if (prefix != null) junit4.addAttribute("prefix", prefix);
    if (debugStream != null) junit4.addAttribute("debugStream", debugStream.toString());
    if (newEnvironment != null) junit4.addAttribute("newEnvironment", newEnvironment.toString());
    if (ifNoTests != null) junit4.addAttribute("ifNoTests", ifNoTests);
    if (statsPropertyPrefix != null) junit4.addAttribute("statsPropertyPrefix", statsPropertyPrefix);
    if (onNonEmptyWorkDirectory != null) junit4.addAttribute("onNonEmptyWorkDirectory", onNonEmptyWorkDirectory);

    junit4.addAttribute("shuffleOnSlave", Boolean.toString(shuffleOnSlave));
    junit4.addAttribute("printSummary", Boolean.toString(printSummary));
    junit4.addAttribute("isolateWorkingDirectories", Boolean.toString(isolateWorkingDirectories));
    junit4.addAttribute("haltOnFailure", Boolean.toString(haltOnFailure));
    junit4.addAttribute("leaveTemporary", Boolean.toString(leaveTemporary));
    junit4.addAttribute("dynamicAssignmentRatio", Float.toString(dynamicAssignmentRatio));
    junit4.addAttribute("sysouts", Boolean.toString(sysouts));
    junit4.addAttribute("uniqueSuiteNames", Boolean.toString(uniqueSuiteNames));


    // JVM args.
    for (String jvmArg : firstNonNull(jvmArgs, EMPTY_STRING_ARRAY)) {
      junit4.addElement("jvmarg").addAttribute("value", jvmArg);
    }
    
    if (argLine != null) {
        junit4.addElement("jvmarg").addAttribute("line", argLine);
    }

    // System properties
    for (Map.Entry<String,String> e : 
      firstNonNull(systemProperties, EMPTY_STRING_STRING_MAP).entrySet()) {
      Element sysproperty = junit4.addElement("sysproperty");
      sysproperty.addAttribute("key", Strings.nullToEmpty(e.getKey()));
      sysproperty.addAttribute("value", Strings.nullToEmpty(e.getValue()));
    }
    
    // Environment variables.
    for (Map.Entry<String,String> e : 
      firstNonNull(environmentVariables, EMPTY_STRING_STRING_MAP).entrySet()) {
      Element sysproperty = junit4.addElement("env");
      sysproperty.addAttribute("key", Strings.nullToEmpty(e.getKey()));
      sysproperty.addAttribute("value", Strings.nullToEmpty(e.getValue()));
    }

    // Tests input.
    setupTestInput(junit4);

    // Tests classpath
    setupTestClasspath(junit4);

    // Copy over listeners configuration.
    if (listeners != null) {
        appendRawXml(listeners, junit4);
    } else {
        // Add a console listener and a surefire-like XML listener.
        Element listenersElement = junit4.addElement("listeners");

        Element surefireReport = listenersElement.addElement("report-ant-xml");
        surefireReport.addAttribute("dir", surefireReportsDirectory.getAbsolutePath());
        surefireReport.addAttribute("mavenExtensions", "true");

        Element consoleReport = listenersElement.addElement("report-text");
        consoleReport.addAttribute("showThrowable",     "true");
        consoleReport.addAttribute("showStackTraces",   "true");
        consoleReport.addAttribute("showOutput",  "never");

        consoleReport.addAttribute("showStatusOk",      "false");
        consoleReport.addAttribute("showStatusError",   "true");
        consoleReport.addAttribute("showStatusFailure", "true");
        consoleReport.addAttribute("showStatusIgnored", "false");
        
        consoleReport.addAttribute("showSuiteSummary",  "true");
    }

    // Copy over runlisteners
    appendRawXml(runListeners, junit4);

    // Copy over assertions
    appendRawXml(assertions, junit4);
    
    // Copy over balancers
    appendRawXml(balancers, junit4);
    
    // Copy over verbatim section.
    if (verbatim != null) {
        for (PlexusConfiguration c : verbatim.getChildren()) {
            appendRawXml(c, junit4);
        }
    }
  }

  /**
   * Append raw XML configuration. 
   */
  private void appendRawXml(PlexusConfiguration config, Element elem) {
    try {
      if (config == null) {
        return;
      }

      StringWriter writer = new StringWriter();
      AntrunXmlPlexusConfigurationWriter xmlWriter = new AntrunXmlPlexusConfigurationWriter();
      xmlWriter.write(config, writer);
      Element root = new SAXReader().read(
          new StringReader(writer.toString())).getRootElement();
      root.detach();
      elem.add(root);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Create a temporary ANT file for executing JUnit4 ANT task.
   */
  private File createTemporaryAntFile(Document doc) throws IOException {
    Closer closer = Closer.create();
    try {
      File antFile = File.createTempFile("junit4-ant-", ".xml", dir);
      OutputStream os = closer.register(new FileOutputStream(antFile));
      XMLWriter xmlWriter = new XMLWriter(os, OutputFormat.createPrettyPrint());
      xmlWriter.write(doc);
      return antFile;
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
  }

  /**
   * Creates a skeleton of a single-target ANT build file with JUnit4 task inside. 
   */
  private Element createDocumentSkeleton(Document doc) {
    Element project = doc.addElement("project");
    project.addAttribute("name", "junit4-maven-synthetic");
    project.addAttribute("default", DEFAULT_TARGET);

    project.addComment("Define JUnit4 task and data types.");
    Element taskdef = project.addElement("taskdef");
    taskdef.addAttribute("resource", JUnit4.ANTLIB_RESOURCE_NAME);
    addArtifactClassPath(
        taskdef.addElement("classpath"),
        pluginArtifactMap.get("com.carrotsearch.randomizedtesting:junit4-ant"));
    addArtifactClassPath(
        taskdef.addElement("classpath"),
        projectArtifactMap.get("junit:junit"));

    project.addComment("Invoke JUnit4 task.");
    Element target = project.addElement("target");
    target.addAttribute("name", DEFAULT_TARGET);

    Element junit4 = target.addElement("junit4");
    return junit4;
  }

  /**
   * Append classpath elements of the given artefact to classpath.
   */
  private void addArtifactClassPath(Element cp, Artifact artifact) {
    ArtifactResolutionResult result = resolveArtifact(artifact);
    if (!result.isSuccess()) { 
      throw new RuntimeException("Could not resolve: " + artifact.toString());
    }
    
    for (Artifact a : result.getArtifacts()) {
        cp.addElement("pathelement").addAttribute(
            "location", a.getFile().getAbsolutePath());
    }
  }

  /** 
   * Resolve a given artifact given exclusion list. (copied from surefire). 
   */
  @SuppressWarnings({"deprecation"})
  private ArtifactResolutionResult resolveArtifact(Artifact artifact, Artifact... filtered) {
    final ArtifactFilter filter;
    if (filtered.length > 0) {
      List<String> exclusions = new ArrayList<>(filtered.length);
      for (Artifact filteredArtifact : filtered) {
        exclusions.add(filteredArtifact.getGroupId() + ":"
            + filteredArtifact.getArtifactId());
      }
      filter = new ExcludesArtifactFilter(exclusions);
    } else {
      filter = null;
    }

    Artifact originatingArtifact = repositorySystem.createArtifact("dummy", "dummy", "1.0", "jar");
    try {
      return resolver.resolveTransitively( 
          Collections.singleton( artifact ), 
          originatingArtifact,
          session.getLocalRepository(),
          project.getPluginArtifactRepositories(), 
          metadataSource, filter);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Setup the classpath used for tests.
   */
  private void setupTestClasspath(Element junit4) {
    junit4.addComment("Runtime classpath.");
    Element cp = junit4.addElement("classpath");

    // Test classes.
    cp.addComment("Test classes directory.");
    cp.addElement("pathelement").addAttribute("location", testClassesDirectory.getAbsolutePath());

    // Classes directory.
    cp.addComment("Test classes directory.");
    cp.addElement("pathelement").addAttribute("location", classesDirectory.getAbsolutePath());

    // Project dependencies.
    cp.addComment("Project dependencies.");

    Set<Artifact> classpathArtifacts = (Set<Artifact>) project.getArtifacts();

    if (!Strings.isNullOrEmpty(classpathDependencyScopeExclude)) {
      classpathArtifacts = filterArtifacts(cp, classpathArtifacts,
          new ScopeArtifactFilter(classpathDependencyScopeExclude));
    }

    if (classpathDependencyExcludes != null && !classpathDependencyExcludes.isEmpty()) {
      classpathArtifacts = filterArtifacts(cp, classpathArtifacts,
          new PatternIncludesArtifactFilter(classpathDependencyExcludes));
    }
    
    for (Artifact artifact : classpathArtifacts) {
      if (artifact.getArtifactHandler().isAddedToClasspath()) {
        File file = artifact.getFile();
        if (file != null) {
          cp.addComment("Dependency artifact: " + artifact.getId());
          cp.addElement("pathelement").addAttribute("location",
              file.getAbsolutePath());
        }
      }
    }
    
    // Additional dependencies.
    cp.addComment("Additional classpath elements.");
    if (additionalClasspathElements != null && !additionalClasspathElements.isEmpty()) {
      for (String classpathElement : additionalClasspathElements) {
        if (!Strings.isNullOrEmpty(classpathElement)) {
          cp.addElement("pathelement").addAttribute("location", classpathElement);
        }
      }
    }
  }

  /**
   * Return a new set containing only the artifacts accepted by the given filter.
   */
  private Set<Artifact> filterArtifacts(Element cp, Set<Artifact> artifacts, ArtifactFilter filter) {
    Set<Artifact> filteredArtifacts = new LinkedHashSet<>();
    for (Artifact artifact : artifacts) {
      if (!filter.include(artifact)) {
        filteredArtifacts.add(artifact);
      } else {
        cp.addComment("Filtered out artifact: " + artifact.getId() + ", location: "
            + artifact.getFile());
      }
    }
    return filteredArtifacts;
  }

  /**
   * Setup the input test suites (classes locations and patterns).  
   */
  private void setupTestInput(Element junit4) {
    Element patternSet = DocumentFactory.getInstance().createElement("patternset");
    for (String includePattern : includes) {
      patternSet.addElement("include").addAttribute("name", includePattern);  
    }
    for (String excludePattern : excludes) {
      patternSet.addElement("exclude").addAttribute("name", excludePattern);  
    }

    if (testClassesDirectory != null) {
      junit4.addComment("Test classes search paths and patterns.");
      Element fs = junit4.addElement("fileset");
      fs.addAttribute("dir", testClassesDirectory.getAbsolutePath());
      fs.addAttribute("erroronmissingdir", "false");
      fs.add(patternSet.createCopy());
    }
  }
}
