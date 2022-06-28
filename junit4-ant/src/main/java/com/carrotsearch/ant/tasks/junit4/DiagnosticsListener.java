package com.carrotsearch.ant.tasks.junit4;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import com.carrotsearch.ant.tasks.junit4.events.BootstrapEvent;
import com.carrotsearch.ant.tasks.junit4.events.IEvent;
import com.carrotsearch.ant.tasks.junit4.events.QuitEvent;
import com.google.common.eventbus.Subscribe;

public class DiagnosticsListener {
  private final ForkedJvmInfo forkedJvmInfo;

  private boolean quitReceived;
  private Task task;

  public DiagnosticsListener(ForkedJvmInfo forkedJvmInfo, JUnit4 task) {
    this.task = task;
    this.forkedJvmInfo = forkedJvmInfo;
  }

  @Subscribe
  public void receiveAll(IEvent e) {
    task.log("Packet received, forkedJvm#" + forkedJvmInfo.id + ">" + e.getType(), Project.MSG_DEBUG);
  }

  @Subscribe
  public void receiveBootstrap(BootstrapEvent e) {
    task.log("Default encoding: " + e.getDefaultCharsetName(), Project.MSG_VERBOSE);
  }

  @Subscribe
  public void receiveQuit(QuitEvent e) {
    quitReceived = true;
  }

  boolean quitReceived() {
    return quitReceived;
  }
}
