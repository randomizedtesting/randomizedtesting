package com.carrotsearch.ant.tasks.junit4;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;

import com.google.common.collect.Lists;

/**
 * A slave process running the actual tests on the target JVM.
 */
public class SlaveMain {
  /**
   * All class names to be executed as tests.
   */
  private final List<String> classes = Lists.newArrayList();

  /**
   * Listeners subscribed to tests execution.
   */
  private final List<IExecutionListener> listeners = Lists.newArrayList();

  /** Stored original system output. */
  private static PrintStream stdout;

  /** Stored original system error. */
  private static PrintStream stderr;

  /**
   * Base for redirected streams. 
   */
  private static class ChunkedStream extends OutputStream {
    public void write(int b) throws IOException {
      throw new IOException("Only write(byte[],int,int) calls expected from super stream.");
    }
  }

  /**
   * Execute tests.
   */
  private void execute() {
    IExecutionListener multiplexer = 
        listenerProxy(Multiplexer.forInterface(IExecutionListener.class, listeners));

    final JUnitCore core = new JUnitCore();
    core.addListener(new StreamFlusher());
    core.addListener(new RunListenerAdapter(multiplexer));
    core.run(instantiate(multiplexer, classes));
  }

  /**
   * Redirect standard streams so that the output can be passed to listeners.
   */
  private static void redirectStreams(final IExecutionListener listener) {
    stdout = System.out;
    stderr = System.err;
    System.setOut(new PrintStream(new BufferedOutputStream(new ChunkedStream() {
      @Override
      public synchronized void write(byte[] b, int off, int len) throws IOException {
        byte [] chunk = new byte [len];
        System.arraycopy(b, off, chunk, 0, len);
        listener.appendOut(chunk);
      }
    })));
    
    System.setOut(new PrintStream(new BufferedOutputStream(new ChunkedStream() {
      @Override
      public synchronized void write(byte[] b, int off, int len) throws IOException {
        byte [] chunk = new byte [len];
        System.arraycopy(b, off, chunk, 0, len);
        listener.appendErr(chunk);
      }
    })));
  }

  private static void restoreStreams() {
    System.out.flush();
    System.err.flush();
    System.setOut(stdout);
    System.setErr(stderr);
  }

  /**
   * Instantiate test classes (or try to).
   */
  private Class<?>[] instantiate(IExecutionListener multiplexer, Collection<String> classnames) {
    final List<Class<?>> instantiated = Lists.newArrayList();
    for (String className : classnames) {
      try {
        instantiated.add(Class.forName(className));
      } catch (Throwable t) {
        warn("Could not instantiate: " + className);
        try {
          multiplexer.testFailure(new Failure(
              Description.createSuiteDescription(className), t));
        } catch (Exception e) {
          warn("Could not report failure: ", t);
        }
      }
    }
    return instantiated.toArray(new Class<?>[instantiated.size()]);
  }

  /**
   * Add classes to be executed as tests.
   */
  public void addTestClasses(String... classnames) {
    this.classes.addAll(Arrays.asList(classnames));
  }

  /**
   * Run listeners to hook to the execution process.
   */
  public void addListeners(IExecutionListener... runListeners) {
    this.listeners.addAll(Arrays.asList(runListeners));
  }

  /**
   * Creates a proxy for {@link IExecutionListener} to a given handler.
   */
  static IExecutionListener listenerProxy(InvocationHandler handler) {
    return (IExecutionListener) Proxy.newProxyInstance(Thread.currentThread()
        .getContextClassLoader(), new Class<?>[] {IExecutionListener.class},                                                 handler);
  }

  /**
   * Warning emitter. 
   */
  private static void warn(String string, Throwable t) {
    stderr.println("WARN: " + string);
    if (t != null) {
      stderr.println("      " + t.toString());
      t.printStackTrace(stderr);
    }
  }

  /**
   * Warning emitter.
   */
  private static void warn(String string) {
    warn(string, null);
  }

  /**
   * Parse command line arguments.
   */
  private static void parseArguments(SlaveMain main, String[] args) {
    for (int i = 0; i < args.length; i++) {
      // The default expectation is a test class.
      main.addTestClasses(args[i]);
    }
  }

  /**
   * Console entry point.
   */
  public static void main(String[] args) throws Exception {
    EventWriter eventWriter = new EventWriter(System.out);
    IExecutionListener listener = listenerProxy(eventWriter);

    redirectStreams(listener);
    int exitStatus = 0;
    try {
      SlaveMain main = new SlaveMain();
      parseArguments(main, args);
      main.addListeners(listener);
      main.execute();
    } catch (Throwable t) {
      warn("Exception at main loop level?", t);
      exitStatus = -1;
    } finally {
      eventWriter.close();
      restoreStreams();
    }
    System.exit(exitStatus);
  }
}
