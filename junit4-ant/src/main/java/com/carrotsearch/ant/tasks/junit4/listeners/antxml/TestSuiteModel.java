package com.carrotsearch.ant.tasks.junit4.listeners.antxml;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Suite model of ANT-JUnit XML.
 */
@Root(name = "testsuite")
public class TestSuiteModel
{
    @Attribute
    public int errors;

    @Attribute
    public int failures;

    /** */
    @Attribute
    public int tests;

    /** The number of skipped tests (maven surefire). */
    @NotAnt(extensionSource = "maven")
    @Attribute(required = false)
    public Integer skipped;

    @Attribute
    public String name;
    
    @Attribute
    public String hostname;

    @Attribute
    public double time;

    @Attribute
    public String timestamp;

    @ElementList(type = PropertyModel.class)
    public List<PropertyModel> properties = new ArrayList<>();

    @ElementList(inline = true, type = TestCaseModel.class)
    public List<TestCaseModel> testcases = new ArrayList<>();

    @Element(name = "system-out", data = true, required = true)
    public String sysout = "";

    @Element(name = "system-err", data = true, required = true)
    public String syserr = "";
}