package com.carrotsearch.ant.tasks.junit4;

import org.apache.tools.ant.Project;

import com.carrotsearch.ant.tasks.junit4.events.BootstrapEvent;
import com.carrotsearch.ant.tasks.junit4.events.QuitEvent;
import com.google.common.eventbus.Subscribe;

public class DiagnosticsListener {
  private final Project project;
  private boolean quitReceived;

  public DiagnosticsListener(Project project) {
    this.project = project;
  }
  
  @Subscribe
  public void receiveBootstrap(BootstrapEvent e) {
    project.log("Communication channel with the slave: " + e.getEventChannel(), Project.MSG_VERBOSE);
    project.log("Default character encoding on the slave: " + e.getDefaultCharsetName(), Project.MSG_VERBOSE);
  }

  @Subscribe
  public void receiveQuit(QuitEvent e) {
    quitReceived = true;
  }

  boolean quitReceived() {
    return quitReceived;
  }
}
