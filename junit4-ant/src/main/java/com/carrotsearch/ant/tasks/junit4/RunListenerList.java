package com.carrotsearch.ant.tasks.junit4;

import com.carrotsearch.ant.tasks.junit4.runlisteners.RunListenerClass;

import java.util.List;

public final class RunListenerList {
    private List<RunListenerClass> listeners;

    public RunListenerList(List<RunListenerClass> listeners) {
        this.listeners = listeners;
    }

    public void addConfigured(RunListenerClass runListener) {
        listeners.add(runListener);
    }
}