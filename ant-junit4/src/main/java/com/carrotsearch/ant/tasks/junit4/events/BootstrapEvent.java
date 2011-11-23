package com.carrotsearch.ant.tasks.junit4.events;

import java.nio.charset.Charset;

/**
 * Initial message sent from the slave to the master (if forked locally).
 */
@SuppressWarnings("serial")
public class BootstrapEvent extends AbstractEvent {
  public static enum EventChannelType {
    STDOUT,
    STDERR,
    SOCKET;
  }

  private EventChannelType eventChannel;
  private String defaultCharset;

  public BootstrapEvent(EventChannelType channelType) {
    super(EventType.BOOTSTRAP);

    if (channelType == EventChannelType.SOCKET) {
      throw new IllegalArgumentException("Not implemented, but possible.");
    }

    this.defaultCharset = Charset.defaultCharset().name();
    this.eventChannel = channelType;
  }

  /**
   * Which channel will be used for communication? We need multiple options
   * because various jvms send crash info to various channels. We will pick
   * accordingly to the slave jvm's capabilities. 
   */
  public EventChannelType getEventChannel() {
    return eventChannel;
  }
  
  public String getDefaultCharsetName() {
    return defaultCharset;
  }
}
