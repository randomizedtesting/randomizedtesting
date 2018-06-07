package com.carrotsearch.ant.tasks.junit4;

import com.carrotsearch.ant.tasks.junit4.runlisteners.UserDefinedRunListener;

import java.util.List;

public class UserDefinedRunListenersList {
    private List<UserDefinedRunListener> userDefinedRunListeners;

    public UserDefinedRunListenersList(List<UserDefinedRunListener> userDefinedRunListeners) {
        this.userDefinedRunListeners = userDefinedRunListeners;
    }

    /**
     * Adds a userDefinedRunListener to the runListener list.
     * @param userDefinedRunListener
     */
    public void addConfigured(UserDefinedRunListener userDefinedRunListener) {
        userDefinedRunListeners.add(userDefinedRunListener);
    }
}