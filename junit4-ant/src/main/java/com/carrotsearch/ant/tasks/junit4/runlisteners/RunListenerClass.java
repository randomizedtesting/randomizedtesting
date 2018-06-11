package com.carrotsearch.ant.tasks.junit4.runlisteners;

public final class RunListenerClass {
    private String className;

    public String getClassName() {
        return className.trim();
    }

    public void setClassName(String className) {
        this.className = className.trim();
    }
}
