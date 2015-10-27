package com.carrotsearch.ant.tasks.junit4.events;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonReader;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonToken;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonWriter;

/**
 * An abstract {@link IEvent}.
 */
abstract class AbstractEvent implements RemoteEvent {
  private final static char [] HEX = "0123456789ABCDEF".toCharArray();

  /** Type is recreated in constructors anyway. */
  private transient final EventType type;

  public AbstractEvent(EventType type) {
    if (this.getClass() != type.eventClass) {
      throw new RuntimeException("Event type mismatch: "
          + type + ", class: " + this.getClass());
    }

    this.type = type;
  }

  @Override
  public EventType getType() {
    return type;
  }

  protected static JsonWriter writeBinaryProperty(JsonWriter writer, String propertyName, byte[] value) throws IOException {
    return writer.name(propertyName).value(toAscii(value));
  }

  protected static byte[] readBinaryProperty(JsonReader reader, String propertyName) throws IOException {
    return fromAscii(expectProperty(reader, propertyName).nextString());
  }

  protected static String readStringProperty(JsonReader reader, String propertyName) throws IOException {
    if (expectProperty(reader, propertyName).peek() != JsonToken.STRING) {
      throw new IOException("Expected a non-null string for property: " + propertyName);
    }
    return reader.nextString();
  }
  
  protected static String readStringOrNullProperty(JsonReader reader, String propertyName) throws IOException {
    expectProperty(reader, propertyName);
    if (reader.peek() == JsonToken.STRING) {
      return reader.nextString();
    } else if (reader.peek() == JsonToken.NULL) {
      reader.nextNull();
      return null;
    } else {
      throw new IOException("Expected a non or string for property: " + propertyName);
    }
  }
  
  protected static boolean readBoolean(JsonReader reader, String propertyName) throws IOException {
    expectProperty(reader, propertyName);
    return reader.nextBoolean();
  }

  protected static long readLongProperty(JsonReader reader, String propertyName) throws IOException {
    return expectProperty(reader, propertyName).nextLong();
  }

  protected static JsonReader expectProperty(JsonReader reader, String propertyName) throws IOException {
    final String name = reader.nextName();
    if (!propertyName.equals(name)) {
      throw new IOException("Expected property: " + propertyName + " but got: " + name);
    }
    return reader;
  }

  private static byte[] fromAscii(String ascii) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    for (int i = 0; i < ascii.length(); i++) {
      char chr = ascii.charAt(i);
      if (chr != '%') {
        baos.write(chr);
      } else {
        baos.write((hexValue(ascii.charAt(++i)) << 4) |
                    hexValue(ascii.charAt(++i)));
      }
    }
    return baos.toByteArray();
  }

  private static int hexValue(char hexChar) throws IOException {
    if (hexChar >= '0' && hexChar <= '9')
      return hexChar - '0';
    if (hexChar >= 'A' && hexChar <= 'F')
      return hexChar - 'A' + 10;
    if (hexChar >= 'a' && hexChar <= 'f')
      return hexChar - 'a' + 10;
    throw new IOException("Unexpected character in binary stream: " + hexChar);
  }

  private static String toAscii(byte[] src) {
    StringBuilder bb = new StringBuilder();
    for (byte b : src) {
      // Pass simple ASCII range.
      if (b >= 32 && b <= 126 && b != '%') {
        bb.append((char) b);
      } else {
        bb.append('%');
        bb.append(HEX[(b >> 4) & 0x0f]);
        bb.append(HEX[(b     ) & 0x0f]);
      }
    }
    return bb.toString();
  }
  
  public static void writeDescription(JsonWriter writer, Description e) throws IOException {
    JsonHelpers.writeDescription(writer, e);
  }  
}
