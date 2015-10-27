package com.carrotsearch.ant.tasks.junit4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.transform.RegistryMatcher;
import org.xml.sax.SAXParseException;

import com.carrotsearch.ant.tasks.junit4.listeners.antxml.XmlStringTransformerAccess;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

public class TestXmlStringsRoundtrip extends RandomizedTest {
  @Test
  @Repeat(iterations = 100)
  public void testRoundTrip() throws Exception {
    char[] chars = new char[randomIntBetween(0, 1024)];
    Random random = getRandom();
    for (int i = 0; i < chars.length; i++) {
      chars[i] = (char) random.nextInt();
    }

    check(chars);
  }

  @Test
  public void testBoundary() throws Exception {
    check(new char [] {'a', 0x0000, 'z'});
    check(new char [] {'a', 0x0001, 'z'});
  }

  @Root
  public static class Model {
    @Attribute
    public String attribute;

    @Element(name = "system-out", data = true, required = true)
    public String contents = "";
    
    public Model() {}
    
    public Model(String s) {
      attribute = contents = s;
    }
  }

  private void check(char[] chars) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
    RegistryMatcher rm = new RegistryMatcher();
    rm.bind(String.class, XmlStringTransformerAccess.getInstance());
    Persister persister = new Persister(rm);
    persister.write(new Model(new String(chars)), baos);

    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    try {
      docBuilder.parse(new ByteArrayInputStream(baos.toByteArray()));
    } catch (SAXParseException e) {
      System.out.println("Input: " + Arrays.toString(chars));
      System.out.println("XML: " + new String(baos.toByteArray(), StandardCharsets.UTF_8));
      throw e;
    }
  }
}
