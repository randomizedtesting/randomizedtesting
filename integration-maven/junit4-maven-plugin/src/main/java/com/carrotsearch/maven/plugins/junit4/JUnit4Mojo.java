package com.carrotsearch.maven.plugins.junit4;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
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
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.artifact.filter.PatternIncludesArtifactFilter;
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
import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;

/**
 * Run tests using a delegation to <a
 * href="https://github.com/carrotsearch/randomizedtesting/">Randomized
 * Testing's JUnit4</a> ANT task.
 * 
 * @requiresProject true
 * @requiresDependencyResolution test
 * @goal junit4
 * @phase test
 * @threadSafe
 */
public class JUnit4Mojo extends AbstractMojo {
  /** An empty String[]. */
  private static final String[] EMPTY_STRING_ARRAY = new String [] {};

  /** An empty Map<String, String>. */
  private static final Map<String,String> EMPTY_STRING_STRING_MAP = Collections.emptyMap();

  /** Default target in the generated ANT file. */
  private static final String DEFAULT_TARGET = "__default__";

  /**
   * The Maven project object
   *
   * @parameter expression="${project}"
   * @readonly
   */
  private MavenProject project;

  /**
   * The directory to invoke the VM in.
   * 
   * @parameter expression="${project.build.directory}"
   */
  private File dir;

  /**
   * The directory to store temporary files in.
   * 
   * @parameter expression="${project.build.directory}"
   */
  private File tempDir;

  /**
   * The number of parallel slaves. Can be set to a constant "max" for the
   * number of cores returned from {@link Runtime#availableProcessors()} or 
   * "auto" for sensible defaults depending on the number of cores.
   * The default is a single subprocess.
   * 
   * <p>Note that this setting forks physical JVM processes so it multiplies the 
   * requirements for heap memory, IO, etc.
   *  
   * @parameter default-value="1"
   */
  private String parallelism = JUnit4.DEFAULT_PARALLELISM;

  /**
   * Property to set to "true" if there is a failure in a test. The use of this
   * property is discouraged in Maven (builds should be declarative).
   *
   * @parameter
   */
  private String failureProperty;

  /**
   * Initial random seed used for shuffling test suites and other sources
   * of pseudo-randomness. If not set, any random value is set. 
   * 
   * <p>The seed's format is compatible with {@link RandomizedRunner} so that
   * seed can be fixed for suites and methods alike.
   * 
   * @parameter expression="${tests.seed}" default-value=""
   */
  private String seed;

  /**
   * Predictably shuffle tests order after balancing. This will help in spreading
   * lighter and heavier tests over a single slave's execution timeline while
   * still keeping the same tests order depending on the seed.
   * 
   * @parameter default-value="true"
   */
  private boolean shuffleOnSlave = JUnit4.DEFAULT_SHUFFLE_ON_SLAVE;
  
  /**
   * Prints the summary of all executed, ignored etc. tests at the end.
   * 
   * @parameter default-value="true"
   */
  private boolean printSummary = JUnit4.DEFAULT_PRINT_SUMMARY;

  /**
   * Stop the build process if there were failures or errors during test execution.
   * 
   * @parameter default-value="true"
   */
  private boolean haltOnFailure = JUnit4.DEFAULT_HALT_ON_FAILURE;

  /**
   * Set the maximum memory to be used by all forked JVMs. The value as 
   * defined by <tt>-mx</tt> or <tt>-Xmx</tt> in the java
   * command line options.
   * 
   * @parameter
   */
  private String maxMemory;

  /**
   * Set to true to leave temporary files for diagnostics.
   * 
   * @parameter 
   */
  private boolean leaveTemporary;

  /**
   * Add an additional argument to any forked JVM.
   * 
   * @parameter
   */
  private String [] jvmArgs;

  /**
   * Adds a system property to any forked JVM.
   * 
   * @parameter
   */
  private Map<String, String> systemProperties; 

  /**
   * Adds an environment variable to any forked JVM.
   * 
   * @parameter
   */
  private Map<String, String> environmentVariables; 

  /**
   * The command used to invoke the Java Virtual Machine, default is 'java'. The
   * command is resolved by java.lang.Runtime.exec().
   * 
   * @parameter default-value="java"
   */
  private String jvm;

  /**
   * The directory containing generated test classes of the project being
   * tested. This will be included at the beginning of the test classpath.
   * 
   * @parameter default-value="${project.build.testOutputDirectory}"
   */
  private File testClassesDirectory;

  /**
   * The directory containing generated classes of the project being
   * tested. This will be included after <code>testClassesDirectory</code>.
   * 
   * @parameter default-value="${project.build.outputDirectory}"
   */
  private File classesDirectory;

  /**
   * A list of &lt;include&gt; elements specifying the tests (by pattern) that should be included in testing. When not
   * specified defaults to: 
   * <pre>
   * &lt;include&gt;**&#47;*Test.class&lt;/include&gt;
   * &lt;include&gt;**&#47;Test*.class&lt;/include&gt;
   * </pre>
   * Note that this may result in nested classes being included for tests. Use proper exclusion patterns. 
   * 
   * @parameter
   */
  private List<String> includes;

  /**
   * A list of &lt;exclude&gt; elements specifying the tests (by pattern) that should be excluded in testing. When not
   * specified defaults to: 
   * <pre>
   * &lt;exclude&gt;**&#47;*$*.class&lt;/exclude&gt;
   * </pre>
   * This patterns excludes any nested classes that might otherwise be included. 
   *
   * @parameter
   */
  private List<String> excludes;

  /**
   * This parameter adds a listener emitting surefire-compatible XMLs if no other listeners
   * are added. If there are any configured listeners, this parameter is omitted (you can
   * add a maven-compatible listener manually).
   * 
   * @parameter default-value="${project.build.directory}/surefire-reports"
   */
  private File surefireReportsDirectory;

  /**
   * Specifies the name of the JUnit artifact used for running tests. JUnit dependency
   * must be in at least version 4.10.
   *
   * @parameter expression="${junitArtifactName}" default-value="junit:junit"
   */
  private String junitArtifactName;

  /**
   * Raw listeners configuration. Same XML as for ANT.
   *
   * @parameter
   */
  private PlexusConfiguration listeners;

  /**
   * Raw assertions configuration. Same XML as for ANT.
   *
   * @parameter
   */
  private PlexusConfiguration assertions;

  /**
   * Raw balancers configuration. Same XML as for ANT.
   *
   * @parameter
   */
  private PlexusConfiguration balancers;

  /**
   * Set this to "true" to skip running tests, but still compile them. Its use
   * is NOT RECOMMENDED, but quite convenient on occasion.
   * 
   * @parameter default-value="false" expression="${skipTests}"
   */
  private boolean skipTests;

  /**
   * List of dependencies to exclude from the test classpath. Each dependency 
   * string must follow the format
   * <i>groupId:artifactId</i>. For example: <i>org.acme:project-a</i>
   * 
   * <p>This is modeled after surefire. An excluded dependency does <b>not</b> mean its
   * transitive dependencies will also be excluded. 
   *
   * @parameter
   */
  private List<String> classpathDependencyExcludes;

  /**
   * A dependency scope to exclude from the test classpath. The scope can be one of the following scopes:
   * <p/>
   * <ul>
   * <li><i>compile</i> - system, provided, compile
   * <li><i>runtime</i> - compile, runtime
   * <li><i>test</i> - system, provided, compile, runtime, test
   * </ul>
   *
   * @parameter default-value=""
   */
  private String classpathDependencyScopeExclude;

  /**
   * Additional elements to be appended to the classpath.
   *
   * @parameter
   */
  private List<String> additionalClasspathElements; 

  /**
   * Map of plugin artifacts.
   *
   * @parameter expression="${plugin.artifactMap}"
   * @required
   * @readonly
   */
  private Map<String,Artifact> pluginArtifactMap;

  /**
   * Map of project artifacts.
   *
   * @parameter expression="${project.artifactMap}"
   * @required
   * @readonly
   */
  private Map<String,Artifact> projectArtifactMap;

  /**
   * The current build session instance.
   *
   * @parameter expression="${session}"
   * @required
   * @readonly
   */
  private MavenSession session;

  /**
   * Repository.
   * 
   * @component
   * @readonly
   * @required
   */
  private RepositorySystem repositorySystem; 

  /**
   * For retrieval of artifact's metadata.
   *
   * @component
   */
  @SuppressWarnings("deprecation")
  private org.apache.maven.artifact.metadata.ArtifactMetadataSource metadataSource;

  /**
   * @component
   */
  private ArtifactResolver resolver;
  
  
  /**
   * Run the mojo.
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    validateParameters();
    
    if (skipTests) {
      return;
    }

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
      antProject.executeTarget(DEFAULT_TARGET);

      if (!leaveTemporary) {
        tempAntFile.delete();
      }
    } catch (IOException e) {
      throw new MojoExecutionException("An I/O error occurred: " + e.getMessage(), e);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Initial validation of input parameters and configuration.
   */
  private void validateParameters() throws MojoExecutionException {
    // Check for junit dependency on project level.
    if (!dir.exists() || !dir.isDirectory()) {
      throw new MojoExecutionException("Directory does not exist: "
          + dir.getAbsolutePath());
    }

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
    junit4.addAttribute("shuffleOnSlave", Boolean.toString(shuffleOnSlave));
    junit4.addAttribute("printSummary", Boolean.toString(printSummary));
    junit4.addAttribute("haltOnFailure", Boolean.toString(haltOnFailure));
    junit4.addAttribute("leaveTemporary", Boolean.toString(leaveTemporary));

    // JVM args.
    for (String jvmArg : Objects.firstNonNull(jvmArgs, EMPTY_STRING_ARRAY)) {
      junit4.addElement("jvmarg").addAttribute("value", jvmArg);
    }

    // System properties
    for (Map.Entry<String,String> e : 
      Objects.firstNonNull(systemProperties, EMPTY_STRING_STRING_MAP).entrySet()) {
      Element sysproperty = junit4.addElement("sysproperty");
      sysproperty.addAttribute("key", Strings.nullToEmpty(e.getKey()));
      sysproperty.addAttribute("value", Strings.nullToEmpty(e.getValue()));
    }
    
    // Environment variables.
    for (Map.Entry<String,String> e : 
      Objects.firstNonNull(environmentVariables, EMPTY_STRING_STRING_MAP).entrySet()) {
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
        consoleReport.addAttribute("showOutputStream",  "false");
        consoleReport.addAttribute("showErrorStream",   "false");

        consoleReport.addAttribute("showStatusOk",      "false");
        consoleReport.addAttribute("showStatusError",   "true");
        consoleReport.addAttribute("showStatusFailure", "true");
        consoleReport.addAttribute("showStatusIgnored", "false");
        
        consoleReport.addAttribute("showSuiteSummary",  "true");
    }

    // Copy over assertions
    appendRawXml(assertions, junit4);
    
    // Copy over balancers
    appendRawXml(balancers, junit4);    
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
    OutputStream os = null;
    try {
      File antFile = File.createTempFile("junit4-ant-", ".xml", dir);
      os = new FileOutputStream(antFile);
      XMLWriter xmlWriter = new XMLWriter(os, OutputFormat.createPrettyPrint());
      xmlWriter.write(doc);
      return antFile;
    } finally {
      Closeables.closeQuietly(os);
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
        pluginArtifactMap.get("com.carrotsearch.randomizedtesting:ant-junit4"));
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
      List<String> exclusions = Lists.newArrayListWithExpectedSize(filtered.length);
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
        if (Strings.isNullOrEmpty(classpathElement)) {
          cp.addElement("pathelement").addAttribute("location",
              classpathElement);
        }
      }
    }
  }

  /**
   * Return a new set containing only the artifacts accepted by the given filter.
   */
  private Set<Artifact> filterArtifacts(Element cp, Set<Artifact> artifacts, ArtifactFilter filter) {
    Set<Artifact> filteredArtifacts = Sets.newLinkedHashSet();
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
      fs.add(patternSet.createCopy());
    }
  }
}
