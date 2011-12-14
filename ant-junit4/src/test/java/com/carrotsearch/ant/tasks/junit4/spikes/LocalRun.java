package com.carrotsearch.ant.tasks.junit4.spikes;

import java.io.File;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.launch.Launcher;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.LoaderUtils;


public class LocalRun {
  public static void main(String[] args) throws Exception {
    Project p = new Project();
    p.init();

    DefaultLogger listener = new DefaultLogger();
    listener.setMessageOutputLevel(Project.MSG_DEBUG);
    listener.setErrorPrintStream(System.err);
    listener.setOutputPrintStream(System.out);
    p.addBuildListener(listener);

    Path antPath = new Path(p);
    antPath.createPathElement().setLocation(sourceOf(Project.class));
    antPath.createPathElement().setLocation(sourceOf(Launcher.class));

    Java java = new Java();
    java.setTaskName("forked");
    java.setProject(p);
    java.setClassname("org.apache.tools.ant.launch.Launcher");
    java.createClasspath().add(antPath);
    java.setFork(true);
    java.setSpawn(false);
    java.setTimeout(10 * 1000L);
    java.setFailonerror(false);

    java.createArg().setLine("-f junit4.xml oldjunit-task");
    java.execute();

    System.out.println("---");
    System.out.println(p.getProperty("output"));
    System.out.println("---");
    System.out.println(p.getProperty("error"));
  }

  private static File sourceOf(Class<?> clazz) {
    return LoaderUtils.getResourceSource(
        clazz.getClassLoader(), clazz.getName().replace('.', '/') + ".class");
  }
}
