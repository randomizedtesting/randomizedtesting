package com.carrotsearch.ant.tasks.junit4.spikes;

import java.io.File;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import com.carrotsearch.ant.tasks.junit4.JUnit4;
import com.carrotsearch.ant.tasks.junit4.listeners.ConsoleReport;
import com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription;
import com.google.common.base.Strings;

public class LocalRun {
  public static void main(String[] args) throws Exception {
    JUnitCore core = new JUnitCore();
    core.addListener(new RunListener() {
      @Override
      public void testRunStarted(Description description) throws Exception {
        dump(description, 0);
      }
      
      @Override
      public void testFailure(Failure failure) throws Exception {
        System.out.println("Failure in:");
        dump(failure.getDescription(), 0);
      }

      private void dump(Description description, int depth) {
        System.out.println(Strings.repeat("  ", depth) + description.getDisplayName());
        for (Description d : description.getChildren()) {
          dump(d, depth + 1);
        }
      }
    });
    core.run(Request.aClass(TestHierarchicalSuiteDescription.class));
    System.exit(0);
    
    Project p = new Project();
    p.init();
    // ProjectHelper.configureProject(p, new File("c:\\carrot2\\carrotsearch.trunk\\carrot2\\build.xml"));
    p.addBuildListener(new DefaultLogger() {
      @Override
      public void messageLogged(BuildEvent e) {
        if (e.getPriority() <= Project.MSG_INFO)
          System.out.println(e.getMessage());
      }
    });
    
    JUnit4 junit4 = new JUnit4();
    junit4.setProject(p);
    junit4.createClasspath().setLocation(new File("../dependency/junit-4.10.jar"));
    junit4.createClasspath().setLocation(new File("../dependency/asm-3.3.1.jar"));
    junit4.createClasspath().setPath("c:\\carrot2\\carrotsearch.trunk\\carrot2\\applications\\carrot2-benchmarks\\lib\\h2-1.2.132.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\applications\\carrot2-benchmarks\\lib\\junit-benchmarks-0.1.0.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\applications\\carrot2-dcs\\lib\\commons-fileupload-1.2.1.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\applications\\carrot2-webapp\\lib\\jawr-2.4.2.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\core\\carrot2-util-attribute\\.apt_factory\\org.carrot2.bindables.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\core\\carrot2-util-text\\lib\\JFlex.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\com.carrotsearch.hppc\\hppc-0.4.1.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\com.carrotsearch.randomizedtesting\\ant-junit4-0.0.3-SNAPSHOT-standalone.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\com.carrotsearch.randomizedtesting\\randomizedtesting-runner-0.0.3-SNAPSHOT.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\com.google.guava\\guava-10.0.1.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\com.google.guava\\jsr305-1.3.9.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\com.planetj.compression\\pjl-comp-filter-1.7.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\com.thoughtworks.qdox\\qdox-1.12.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\javax.servlet\\servlet-api-2.5.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\morfologik\\morfologik-stemming-1.5.1.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\net.java.dev.rome\\rome-1.0.0.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\net.java.dev.rome\\rome-fetcher-1.0.0.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\net.sf.ehcache\\ehcache-core-1.7.2.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.commons\\commons-codec-1.4.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.commons\\commons-collections-3.2.1.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.commons\\commons-io-2.0.1.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.commons\\commons-lang-2.6.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.http\\httpclient-4.1.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.http\\httpcore-4.1.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.http\\httpmime-4.1.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.log4j\\log4j-1.2.16.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.lucene\\lucene-analyzers-3.4.0.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.lucene\\lucene-core-3.4.0.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.lucene\\lucene-highlighter-3.4.0.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.lucene\\lucene-memory-3.4.0.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.lucene\\lucene-smartcn-3.4.0.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.mahout\\mahout-collections-1.0.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.mahout\\mahout-math-0.5.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.oro\\oro-2.0.8.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.velocity\\velocity-1.7.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.velocity\\velocity-tools-generic-2.0.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.xml\\resolver.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.xml\\serializer.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.xml\\xalan-2.7.1.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.xml\\xercesImpl-2.11.0.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.apache.xml\\xml-apis.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.carrot2.antlib\\org.carrot2.antlib.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.codehaus.jackson\\jackson-core-asl-1.7.4.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.codehaus.jackson\\jackson-mapper-asl-1.7.4.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.easymock\\easymock-3.0.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.eclipse.jetty\\jetty-continuation-7.3.1.v20110307.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.eclipse.jetty\\jetty-deploy-7.3.1.v20110307.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.eclipse.jetty\\jetty-http-7.3.1.v20110307.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.eclipse.jetty\\jetty-io-7.3.1.v20110307.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.eclipse.jetty\\jetty-security-7.3.1.v20110307.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.eclipse.jetty\\jetty-server-7.3.1.v20110307.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.eclipse.jetty\\jetty-servlet-7.3.1.v20110307.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.eclipse.jetty\\jetty-servlets-7.3.1.v20110307.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.eclipse.jetty\\jetty-util-7.3.1.v20110307.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.eclipse.jetty\\jetty-webapp-7.3.1.v20110307.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.eclipse.jetty\\jetty-xml-7.3.1.v20110307.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.fest\\fest-assert-1.4.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.fest\\fest-mocks-1.0.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.fest\\fest-util-1.1.6.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.gargoylesoftware.htmlunit\\cssparser-0.9.5.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.gargoylesoftware.htmlunit\\htmlunit-2.9-SNAPSHOT.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.gargoylesoftware.htmlunit\\htmlunit-core-js-2.9-SNAPSHOT.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.gargoylesoftware.htmlunit\\nekohtml-1.9.15-20101026.093020-2.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.gargoylesoftware.htmlunit\\sac-1.3.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.jdom\\jdom-1.1.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.junit4-ext\\junit-4.10.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.kohsuke.args4j\\args4j-2.0.9.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.simpleframework.xml\\simple-xml-2.4.1.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.slf4j\\jcl-over-slf4j-1.6.1.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.slf4j\\slf4j-api-1.6.1.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\lib\\org.slf4j\\slf4j-log4j12-1.6.1.jar;c:\\carrot2\\carrotsearch.trunk\\carrot2\\core\\carrot2-component-suites\\suites;c:\\carrot2\\carrotsearch.trunk\\carrot2\\core\\carrot2-util-log4j\\src;c:\\carrot2\\carrotsearch.trunk\\carrot2\\tmp\\classes-test;c:\\carrot2\\carrotsearch.trunk\\carrot2\\tmp\\classes;c:\\carrot2\\resources\\tools\\clover-ant-2.6.3\\lib\\clover.jar");
    junit4.createClasspath().setLocation(new File("."));
    junit4.setParallelism("2");

    ConsoleReport report = new ConsoleReport();
    report.setShowErrors(true);
    report.setShowStackTraces(false);
    report.setShowOutputStream(true);
    report.setShowErrorStream(true);
    junit4.createListeners().addConfigured(report);

    FileSet fs = new FileSet();
    fs.setDir(new File("."));
    fs.setIncludes("**/Test*.class");
    fs.setExcludes("**/*$*");
    fs.setExcludes("**/TestJvmCrash.class");
    junit4.addFileSet(fs);
    junit4.execute();
  }
}
