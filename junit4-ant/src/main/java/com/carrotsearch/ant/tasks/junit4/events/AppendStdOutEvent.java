package com.carrotsearch.ant.tasks.junit4.events;

import java.io.IOException;
import java.io.OutputStream;

import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonReader;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonWriter;

public class AppendStdOutEvent extends AbstractEvent implements IStreamEvent {
  private byte[] chunk;

  protected AppendStdOutEvent() {
    super(EventType.APPEND_STDOUT);
  }

  public AppendStdOutEvent(byte[] b, int off, int len) {
    this();
    chunk = new byte [len];
    System.arraycopy(b, off, chunk, 0, len);
  }

  @Override
  public void copyTo(OutputStream os) throws IOException {
    os.write(chunk);
  }
  
  @Override
  public void serialize(JsonWriter writer) throws IOException {
    writer.beginObject();
    writeBinaryProperty(writer, "chunk", chunk);
    writer.endObject();
  }

  @Override
  public void deserialize(JsonReader reader) throws IOException {
    reader.beginObject();
    chunk = readBinaryProperty(reader, "chunk");
    reader.endObject();
  }  
}
