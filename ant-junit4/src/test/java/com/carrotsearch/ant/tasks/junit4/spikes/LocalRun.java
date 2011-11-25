package com.carrotsearch.ant.tasks.junit4.spikes;

import java.io.File;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.FileSet;

import com.carrotsearch.ant.tasks.junit4.JUnit4;
import com.carrotsearch.ant.tasks.junit4.listeners.ConsoleReport;

public class LocalRun {
  public static void main(String[] args) {
    Project p = new Project();
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
    junit4.createClasspath().setLocation(new File("."));
    junit4.setParallellism("4");

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
