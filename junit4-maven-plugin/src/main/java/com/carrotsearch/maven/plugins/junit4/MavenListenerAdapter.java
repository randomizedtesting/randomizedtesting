package com.carrotsearch.maven.plugins.junit4;

import org.apache.maven.plugin.logging.Log;
import org.apache.tools.ant.*;

/**
 * An adapter to maven logging system from ANT {@link BuildListener}.
 */
final class MavenListenerAdapter implements BuildListener {
  private final Log log;

  public MavenListenerAdapter(Log log) {
    this.log = log;
  }

  @Override
  public void messageLogged(BuildEvent event) {
    final Throwable exception = event.getException();
    final String message = event.getMessage();
    switch (event.getPriority()) {
      case Project.MSG_DEBUG:
      case Project.MSG_VERBOSE:
        if (exception != null) {
          log.debug(message, exception);
        } else {
          log.debug(message);
        }
        break;

      case Project.MSG_INFO:
        if (exception != null) {
          log.info(message, exception);
        } else {
          log.info(message);
        }
        break;

      case Project.MSG_WARN:
        if (exception != null) {
          log.warn(message, exception);
        } else {
          log.warn(message);
        }
        break;
        
      case Project.MSG_ERR:
        if (exception != null) {
          log.error(message, exception);
        } else {
          log.error(message);
        }
        break;        
    }
  }

  @Override
  public void buildFinished(BuildEvent event) {}
  
  @Override
  public void buildStarted(BuildEvent event) {}

  @Override
  public void targetFinished(BuildEvent event) {}
  
  @Override
  public void targetStarted(BuildEvent event) {}
  
  @Override
  public void taskFinished(BuildEvent event) {}
  
  @Override
  public void taskStarted(BuildEvent event) {}
}
