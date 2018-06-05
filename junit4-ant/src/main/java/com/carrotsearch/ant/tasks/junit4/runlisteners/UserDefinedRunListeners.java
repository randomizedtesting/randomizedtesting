package com.carrotsearch.ant.tasks.junit4.runlisteners;

import org.junit.runner.notification.RunListener;

import java.util.ArrayList;
import java.util.Arrays;

public class UserDefinedRunListeners {

    public static ArrayList<RunListener> generateInstances() throws Exception {
        ArrayList<RunListener> instances = new ArrayList<>();

        String userDefinedRunListeners = System.getProperty("userDefinedRunListeners");

        if(userDefinedRunListeners != null) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            for (String className : Arrays.asList(userDefinedRunListeners.split(","))) {
                instances.add(RunListener.class.cast(classLoader.loadClass(className).newInstance()));
            }
        }

        return instances;
    }

}
