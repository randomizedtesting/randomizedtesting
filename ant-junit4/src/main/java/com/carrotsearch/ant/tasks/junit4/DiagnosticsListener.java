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
    log("Packet received: " + e.getType());
  }

  private void log(String message) {
    project.log("slave#" + slave.id + ">" + message, Project.MSG_VERBOSE);
  }

  @Subscribe
  public void receiveBootstrap(BootstrapEvent e) {
    log("Communication channel: " + e.getEventChannel() + ", " +
        "Default encoding: " + e.getDefaultCharsetName());
    slave.setCharset(e.getDefaultCharsetName());
  }

  @Subscribe
  public void receiveQuit(QuitEvent e) {
    quitReceived = true;
  }

  boolean quitReceived() {
    return quitReceived;
  }
}
