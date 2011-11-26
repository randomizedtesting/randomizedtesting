package com.carrotsearch.ant.tasks.junit4;

import org.apache.tools.ant.Project;

import com.carrotsearch.ant.tasks.junit4.events.BootstrapEvent;
import com.carrotsearch.ant.tasks.junit4.events.IEvent;
import com.carrotsearch.ant.tasks.junit4.events.QuitEvent;
import com.google.common.eventbus.Subscribe;

public class DiagnosticsListener {
  private final Project project;
  private final SlaveInfo slave;

  private boolean quitReceived;

  public DiagnosticsListener(SlaveInfo slave, Project project) {
    this.project = project;
    this.slave = slave;
  }

  @Subscribe
  public void receiveAll(IEvent e) {
    project.log("Packet received, slave#" + slave.id + ">" + e.getType(), Project.MSG_DEBUG);
  }

  @Subscribe
  public void receiveBootstrap(BootstrapEvent e) {
    slave.start = System.currentTimeMillis();
    project.log("Communication channel: " + e.getEventChannel() + ", " +
        "Default encoding: " + e.getDefaultCharsetName(), Project.MSG_VERBOSE);
    slave.setBootstrapEvent(e);
  }

  @Subscribe
  public void receiveQuit(QuitEvent e) {
    quitReceived = true;
    slave.end = System.currentTimeMillis();
  }

  boolean quitReceived() {
    return quitReceived;
  }
}
