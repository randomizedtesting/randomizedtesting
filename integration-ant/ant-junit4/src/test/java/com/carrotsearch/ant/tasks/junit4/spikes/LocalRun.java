package com.carrotsearch.ant.tasks.junit4.spikes;

import java.io.File;
import java.util.Hashtable;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class LocalRun {
  public static void main(String[] args) throws Exception {
    Project p = new Project();
    p.init();
    
    File file = File.createTempFile("temp", ".txt", new File("."));
    try {
      Files.write("<project name='temp' default='empty'>" +
      		"<fileset dir='.' id='fset' />" +
          "<target name='empty'><echo>abc</echo></target>" + 
          "</project>", file, Charsets.UTF_8);

      ProjectHelper.configureProject(p, file);
      Hashtable tasks = p.getTaskDefinitions();
      
      p.executeTarget("empty");
     
      System.out.println("Project: " + p.getReference("fset"));
    } finally {
      file.delete();
    }
  }
}
