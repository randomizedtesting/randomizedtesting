package com.carrotsearch.ant.tasks.junit4.listeners.antxml;

import org.simpleframework.xml.transform.Transform;

final class XmlStringTransformer implements Transform<String> {
  private final StringBuilder buffer = new StringBuilder();

  @Override
  public String read(String value) throws Exception {
    return value;
  }

  @Override
  public String write(String value) throws Exception {
    if (!isMappableXmlText(value)) {
      return remap(value);
    }
    return value;
  }

  private String remap(CharSequence value) {
    buffer.setLength(0);
    final int length = value.length();
    for (int i = 0; i < length; i = Character.offsetByCodePoints(value, i, 1)) {
      int cp = Character.codePointAt(value, i);
      if ((cp >= 0x20 && cp <= 0x00D7FF) || 
          (cp < 0x20 && (cp == 0x09 || cp == 0x0A || cp == 0x0D)) || 
          (cp >= 0xE000 && cp <= 0x00FFFD) || 
          (cp >= 0x10000 && cp <= 0x10FFFF)) {
        buffer.appendCodePoint(cp);
      } else {
        buffer.append(/* Replacement char. */ "\ufffd");
      }
    }
    return buffer.toString();
  }

  private static boolean isMappableXmlText(CharSequence value) {
    final int length = value.length();
    for (int i = 0; i < length; i = Character.offsetByCodePoints(value, i, 1)) {
      int cp = Character.codePointAt(value, i);
      if ((cp >= 0x20 && cp <= 0x00D7FF) || 
          (cp < 0x20 && (cp == 0x09 || cp == 0x0A || cp == 0x0D)) || 
          (cp >= 0xE000 && cp <= 0x00FFFD) || 
          (cp >= 0x10000 && cp <= 0x10FFFF)) {
        // Ok, mappable XML character.
      } else {
        return false;
      }
    }
    return true;
  }
}
