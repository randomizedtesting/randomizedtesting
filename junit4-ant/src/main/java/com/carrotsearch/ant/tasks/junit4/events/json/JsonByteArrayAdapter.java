package com.carrotsearch.ant.tasks.junit4.events.json;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;

import com.google.gson.*;

/**
 * Serialize byte array to portable ASCII. This is used primarily to carry system streams
 * and since the encoding (or correctness) of these is generally not known we encode them
 * as binary to recover them faithfully. 
 */
public class JsonByteArrayAdapter 
  implements JsonSerializer<byte[]>,
             JsonDeserializer<byte[]>
{
  private final static char [] HEX = "0123456789ABCDEF".toCharArray();
  private final StringBuilder bb = new StringBuilder();
  private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

  @Override
  public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(toAscii(src));
  }

  private String toAscii(byte[] src) {
    bb.setLength(0);
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
    baos.reset();
    for (int i = 0; i < input.length(); i++) {
      char chr = input.charAt(i);
      if (chr != '%') {
        baos.write(chr);
      } else {
        baos.write((hexValue(input.charAt(++i)) << 4) |
                    hexValue(input.charAt(++i)));
      }
    }
    return baos.toByteArray();
  }
}