package com.carrotsearch.ant.tasks.junit4.listeners.antxml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Text;

public class FailureModel {
  @Attribute
  public String message;

  @Attribute
  public String type;

  @Text
  public String text;
}
