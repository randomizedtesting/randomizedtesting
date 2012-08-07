package com.carrotsearch.ant.tasks.junit4.it;


import java.util.HashMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;


public class TestPickSeed extends JUnit4XmlTestBase {
  
  @Test
  public void pickSeed() {
    for (int i = 0; i < 7; i++) {
      super.executeTarget("randomsysproperty");
    }

    HashMap<String, TreeSet<String>> props = new HashMap<String, TreeSet<String>>();  

    for (String key : new String [] {
        "prefix.dummy1",
        "prefix.dummy2",
        "replaced.dummy1",
        "replaced.dummy2",
    }) {
      TreeSet<String> values = new TreeSet<String>();
      Pattern p = Pattern.compile("(?:> )(" + key + ")(?:=)([^\\s]+)");
      Matcher m = p.matcher(getLog());
      while (m.find()) {
        values.add(m.group(2));
      }
      
      props.put(key, values);
    }
    
    Assert.assertEquals(props.get("prefix.dummy1"), props.get("replaced.dummy1"));
    Assert.assertEquals(props.get("prefix.dummy2"), props.get("replaced.dummy2"));

    // At least two unique values.
    Assert.assertTrue(props.get("prefix.dummy1").size() >= 2);
    // null (missing value) should be there.
    Assert.assertTrue(props.get("prefix.dummy2").contains("null"));
  }
}
