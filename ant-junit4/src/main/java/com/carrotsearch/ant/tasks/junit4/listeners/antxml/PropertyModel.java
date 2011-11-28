package com.carrotsearch.ant.tasks.junit4.listeners.antxml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import com.google.common.base.Strings;

@Root(name = "property")
public class PropertyModel {
  @Attribute(required = true)
  public String name;

  @Attribute(required = true)
  public String value;

  public PropertyModel(String key, String value) {
    this.name = Strings.nullToEmpty(key);
    this.value = Strings.nullToEmpty(value);
  }
}
