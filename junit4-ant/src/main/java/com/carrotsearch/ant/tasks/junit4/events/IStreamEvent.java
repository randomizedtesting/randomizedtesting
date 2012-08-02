package com.carrotsearch.ant.tasks.junit4.events;

import java.nio.ByteBuffer;

public interface IStreamEvent {
  public ByteBuffer getChunk();
}
