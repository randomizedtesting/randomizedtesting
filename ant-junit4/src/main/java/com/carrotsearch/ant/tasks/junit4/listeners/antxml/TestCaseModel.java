package com.carrotsearch.ant.tasks.junit4.listeners.antxml;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import com.google.common.collect.Lists;

@Root(name = "testcase")
public class TestCaseModel
{
    @Attribute(required = true)
    public String classname;

    @Attribute(required = true)
    public String name;

    @Attribute(required = true)
    public double time;

    @ElementList(inline = true, entry = "failure", required = false, type = FailureModel.class)
    public List<FailureModel> failures = Lists.newArrayList();

    @ElementList(inline = true, entry = "error", required = false, type = FailureModel.class)
    public List<FailureModel> errors = Lists.newArrayList();
}