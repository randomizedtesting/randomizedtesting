package com.carrotsearch.ant.tasks.junit4.events.json;

import java.lang.reflect.Type;

import com.google.gson.*;

/**
 * Byte array as hexadecimal ASCII byte dump.
 */
public class JsonByteArrayAdapter 
  implements JsonSerializer<byte[]>,
             JsonDeserializer<byte[]>
{
  private final static char [] HEX = "0123456789ABCDEF".toCharArray();

  @Override
  public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(toHex(src));
  }

  private String toHex(byte[] src) {
    StringBuilder bb = new StringBuilder(src.length * 2);
    for (byte b : src) {
      bb.append(HEX[(b >> 4) & 0x0f]);
      bb.append(HEX[(b     ) & 0x0f]);
    }
    return bb.toString();
  }

  private int hexValue(char hexChar) throws JsonParseException {
    if (hexChar >= '0' && hexChar <= '9')
      return hexChar - '0';
    if (hexChar >= 'A' && hexChar <= 'F')
      return hexChar - 'A' + 10;
    if (hexChar >= 'a' && hexChar <= 'f')
      return hexChar - 'a' + 10;
    throw new JsonParseException("Unexpected character in binary stream: " + hexChar);
  }

  @Override
  public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    if (typeOfT.equals(byte[].class))
      throw new JsonParseException("Not a byte[]: " + typeOfT);
    String input = json.getAsString();
    byte [] output = new byte [input.length() / 2];
    for (int i = 0, j = 0; i < input.length(); i += 2, j++) {
      output[j] = (byte) (
               hexValue(input.charAt(i)) << 4 |
               hexValue(input.charAt(i + 1)));
    }
    return output;
  }
}