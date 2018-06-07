package com.carrotsearch.ant.tasks.junit4.runlisteners;

public class UserDefinedRunListener {
    private String className = null;

    public String getClassName() {
        return className.trim();
    }

    public void setClassName(String className) {
        this.className = className.trim();
    }
}
