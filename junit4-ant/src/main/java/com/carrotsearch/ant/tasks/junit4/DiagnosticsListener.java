package com.carrotsearch.ant.tasks.junit4;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import com.carrotsearch.ant.tasks.junit4.events.BootstrapEvent;
import com.carrotsearch.ant.tasks.junit4.events.IEvent;
import com.carrotsearch.ant.tasks.junit4.events.QuitEvent;
import com.google.common.eventbus.Subscribe;

public class DiagnosticsListener {
  private final SlaveInfo slave;

  private boolean quitReceived;
  private Task task;

  public DiagnosticsListener(SlaveInfo slave, JUnit4 task) {
    this.task = task;
    this.slave = slave;
  }

  @Subscribe
  public void receiveAll(IEvent e) {
    task.log("Packet received, slave#" + slave.id + ">" + e.getType(), Project.MSG_DEBUG);
  }

  @Subscribe
  public void receiveBootstrap(BootstrapEvent e) {
    slave.start = System.currentTimeMillis();
    task.log("Communication channel: " + e.getEventChannel() + ", " +
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
