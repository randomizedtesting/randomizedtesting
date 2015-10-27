package com.carrotsearch.ant.tasks.junit4.events;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonReader;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonToken;
import com.carrotsearch.ant.tasks.junit4.gson.stream.JsonWriter;

/**
 * Initial message sent from the slave to the master (if forked locally).
 */
public class BootstrapEvent extends AbstractEvent {
  private static final List<String> GUARANTEED_PROPERTIES = Arrays.asList(
      "java.version",
      "java.vendor",
      "java.vendor.url",
      "java.home",
      "java.vm.specification.version",
      "java.vm.specification.vendor",
      "java.vm.specification.name",
      "java.vm.version",
      "java.vm.vendor",
      "java.vm.name",
      "java.specification.version",
      "java.specification.vendor",
      "java.specification.name",
      "java.class.version",
      "java.class.path",
      "java.library.path",
      "java.io.tmpdir",
      "java.compiler",
      "java.ext.dirs",
      "os.name",
      "os.arch",
      "os.version",
      "file.separator",
      "path.separator",
      "line.separator",
      "user.name",
      "user.home",
      "user.dir");

  private String defaultCharset;
  private Map<String, String> systemProperties;
  private String pidString;

  /** Preinitialization with local machine's configuration. */
  public BootstrapEvent() {
    super(EventType.BOOTSTRAP);

    this.defaultCharset = Charset.defaultCharset().name();

    try {
      pidString = ManagementFactory.getRuntimeMXBean().getName();
    } catch (Throwable t) {
      pidString = "<pid acquire exception: " + t.toString() + ">";
    }

    this.systemProperties = collectSystemProperties();

    systemProperties.put("junit4.memory.total", 
        Long.toString(Runtime.getRuntime().totalMemory()));
    systemProperties.put("junit4.processors", 
        Long.toString(Runtime.getRuntime().availableProcessors()));
    systemProperties.put("junit4.pidString", pidString);
  }

  private Map<String,String> collectSystemProperties() {
    List<String> propertyNames = new ArrayList<String>();
    try {
      Enumeration<?> e = System.getProperties().propertyNames();
      while (e.hasMoreElements()) {
        propertyNames.add((String) e.nextElement()); 
      }
    } catch (SecurityException e) {
      // No access to the full set of properties. Try to include at least the default
      // guaranteed set of properties (maybe we have read-only access).
      propertyNames.addAll(GUARANTEED_PROPERTIES);
    }

    TreeMap<String, String> sysProps = new TreeMap<String, String>();
    for (String propertyName : propertyNames) {
      try {
        String value = System.getProperty(propertyName);
        if (value != null) {
          sysProps.put(propertyName, value);
        }
      } catch (SecurityException e) {
        // No access. Ignore.
      }
    }

    return sysProps;
  }

  /**
   * Default charset on the slave.
   */
  public String getDefaultCharsetName() {
    return defaultCharset;
  }

  /**
   * System properties on the slave.
   */
  public Map<String,String> getSystemProperties() {
    return systemProperties;
  }

  /**
   * Returns a PID string or anything that approximates it and would
   * help in dumping a stack trace externally, for example.
   */
  public String getPidString() {
    return pidString;
  }
  
  @Override
  public void serialize(JsonWriter writer) throws IOException {
    writer.beginObject();
    writer.name("defaultCharset").value(defaultCharset);
    writer.name("pidString").value(pidString);

    writer.name("systemProperties").beginObject();
    for (Map.Entry<String,String> e : systemProperties.entrySet()) {
      writer.name(e.getKey()).value(e.getValue());
    }
    writer.endObject();
    
    writer.endObject();
  }

  @Override
  public void deserialize(JsonReader reader) throws IOException {
    reader.beginObject();
    defaultCharset = readStringProperty(reader, "defaultCharset");
    pidString = readStringProperty(reader, "pidString");

    expectProperty(reader, "systemProperties");
    reader.beginObject();
    systemProperties = new LinkedHashMap<String,String>();
    while (reader.peek() != JsonToken.END_OBJECT) {
      systemProperties.put(reader.nextName(), reader.nextString());
    }
    reader.endObject();

    reader.endObject();
  }
}
