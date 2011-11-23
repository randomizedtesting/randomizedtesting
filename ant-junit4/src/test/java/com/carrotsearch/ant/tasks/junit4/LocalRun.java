package com.carrotsearch.ant.tasks.junit4;

import java.io.File;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.FileSet;

public class LocalRun {
  public static void main(String[] args) {
    Project p = new Project();
    p.addBuildListener(new DefaultLogger() {
      @Override
      public void messageLogged(BuildEvent e) {
        System.out.println(e.getMessage());
      }
    });

    JUnit4 junit4 = new JUnit4();
    junit4.setProject(p);
    junit4.createClasspath().setLocation(new File("../dependency/junit-4.10.jar"));
    junit4.createClasspath().setLocation(new File("."));
    
    FileSet fs = new FileSet();
    fs.setDir(new File("."));
    fs.setIncludes("**/TestJvmCrash.class");
    junit4.addFileSet(fs);
    junit4.execute();
  }
}
