package com.carrotsearch.ant.tasks.junit4.slave;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import com.carrotsearch.ant.tasks.junit4.JUnit4;
import com.carrotsearch.ant.tasks.junit4.events.AppendStdErrEvent;
import com.carrotsearch.ant.tasks.junit4.events.AppendStdOutEvent;
import com.carrotsearch.ant.tasks.junit4.events.BootstrapEvent;
import com.carrotsearch.ant.tasks.junit4.events.BootstrapEvent.EventChannelType;
import com.carrotsearch.ant.tasks.junit4.events.QuitEvent;
import com.carrotsearch.ant.tasks.junit4.events.Serializer;
import com.carrotsearch.ant.tasks.junit4.events.SuiteFailureEvent;
import com.carrotsearch.randomizedtesting.MethodGlobFilter;

import com.google.common.base.Strings;

/**
 * A slave process running the actual tests on the target JVM.
 */
public class SlaveMain {
  /** Runtime exception. */
  public static final int ERR_EXCEPTION = 255;

  /** No JUnit on classpath. */
  public static final int ERR_NO_JUNIT = 254;

  /** Old JUnit on classpath. */
  public static final int ERR_OLD_JUNIT = 253;

  /**
   * Frequent event strean flushing.
   */
  public static final String OPTION_FREQUENT_FLUSH = "-flush";


  /**
   * All class names to be executed as tests.
   */
  private final List<String> classes = new ArrayList<String>();

  /**
   * Event sink.
   */
  private final Serializer serializer;

  /** Stored original system output. */
  private static PrintStream stdout;

  /** Stored original system error. */
  private static PrintStream stderr;

  /** A sink for warnings (non-event stream). */
  private static PrintStream warnings;

  /** Flush serialization stream frequently. */
  private boolean flushFrequently = false;

  /**
   * Base for redirected streams. 
   */
  private static class ChunkedStream extends OutputStream {
    public void write(int b) throws IOException {
      throw new IOException("Only buffered write(byte[],int,int) calls expected from super stream.");
    }
  }

  /**
   * Creates a slave emitting events to the given serializer.
   */
  public SlaveMain(Serializer serializer) {
    this.serializer = serializer;
  }

  /**
   * Execute tests.
   */
  private void execute() {
    final JUnitCore core = new JUnitCore();
    core.addListener(
        new StreamFlusherDecorator(
            new NoExceptionRunListenerDecorator(new RunListenerEmitter(serializer)) {
              @Override
              protected void exception(Throwable t) {
                warn("Event serializer exception.", t);
              }
            }));

    if (flushFrequently) {
      core.addListener(new RunListener() {
        public void testRunFinished(Result result) throws Exception {
          serializer.flush();
        }
        
        public void testFinished(Description description) throws Exception {
          serializer.flush();
        }
      });
    }

    /*
     * Instantiate method filter if any.
     */
    String methodFilterGlob = Strings.emptyToNull(System.getProperty(JUnit4.PROP_TESTMETHOD));
    Filter methodFilter = Filter.ALL;
    if (methodFilterGlob != null) {
      methodFilter = new MethodGlobFilter(methodFilterGlob);
    }

    /*
     * Important. Run each class separately so that we get separate 
     * {@link RunListener} callbacks for the top extracted description.
     */
    for (Class<?> suite : instantiate(classes)) {
      Request request = Request.aClass(suite);
      try {
        Runner runner= request.getRunner();
        methodFilter.apply(runner);
        core.run(runner);        
      } catch (NoTestsRemainException e) {
        // Don't complain if all methods have been filtered out. 
        // I don't understand the reason why this exception has been
        // built in to filters at all.
      }
    }
  }

  /**
   * Instantiate test classes (or try to).
   */
  private Class<?>[] instantiate(Collection<String> classnames) {
    final List<Class<?>> instantiated = new ArrayList<Class<?>>();
    for (String className : classnames) {
      try {
        instantiated.add(Class.forName(className));
      } catch (Throwable t) {
        try {
          serializer.serialize(
              new SuiteFailureEvent(
                  new Failure(Description.createSuiteDescription(className), t)));
          if (flushFrequently)
            serializer.flush();
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
   * Console entry point.
   */
  public static void main(String[] args) {
    int exitStatus = 0;
    Serializer serializer = null;
    try {
      // Pick the communication channel.
      final BootstrapEvent.EventChannelType channel = establishCommunicationChannel(); 
      new Serializer(System.out)
        .serialize(new BootstrapEvent(channel))
        .flush();

      final int bufferSize = 16 * 1024;
      switch (channel) {
        case STDERR:
          serializer = new Serializer(new BufferedOutputStream(System.err, bufferSize));
          warnings = System.out;
          break;

        case STDOUT:
          serializer = new Serializer(new BufferedOutputStream(System.out, bufferSize));
          warnings = System.err;
          break;

        default:
          warnings = System.err;
          throw new RuntimeException("Communication not implemented: " + channel);
      }

      // Redirect original streams and start running tests.
      redirectStreams(serializer);
      final SlaveMain main = new SlaveMain(serializer);
      parseArguments(main, args);
      main.execute();
    } catch (Throwable t) {
      warn("Exception at main loop level?", t);
      exitStatus = -1;
    } finally {
      restoreStreams();
    }

    if (serializer != null) {
      try {
        serializer.serialize(new QuitEvent());
        serializer.getOutputStream().close();
      } catch (IOException e) {
        // Ignore.
      }
    }

    System.exit(exitStatus);
  }

  /**
   * Parse command line arguments.
   */
  private static void parseArguments(SlaveMain main, String[] args) throws IOException {
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals(OPTION_FREQUENT_FLUSH)) {
        main.flushFrequently = true;
      } else if (args[i].startsWith("@")) {
        // Arguments file, one line per option.
        parseArguments(main, readArgsFile(args[i].substring(1)));
      } else {
        // The default expectation is a test class.
        main.addTestClasses(args[i]);
      }
    }
  }

  /**
   * Read arguments from a file. Newline delimited, UTF-8 encoded. No fanciness to 
   * avoid dependencies.
   */
  private static String[] readArgsFile(String argsFile) throws IOException {
    final ArrayList<String> lines = new ArrayList<String>();
    final BufferedReader reader = new BufferedReader(
        new InputStreamReader(
            new FileInputStream(argsFile), "UTF-8"));
    try {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (!line.isEmpty() && !line.startsWith("#")) {
          lines.add(line);
        }
      }
    } finally {
      reader.close();
    }
    return lines.toArray(new String [lines.size()]);
  }

  /**
   * Establish communication channel based on the JVM type.
   */
  private static EventChannelType establishCommunicationChannel() {
    String vmName = System.getProperty("java.vm.name");
    // Default event channel: stderr. stdout is used for vm crash info.
    EventChannelType eventChannel = EventChannelType.STDERR;
    if (vmName != null) {
      // These use stderr in case of jvm crash.
      if (vmName.contains("JRockit")) {
        return BootstrapEvent.EventChannelType.STDOUT;
      } else if (vmName.contains("J9")) {
        return BootstrapEvent.EventChannelType.STDOUT;
      }
    }
    return eventChannel;
  }

  /**
   * Redirect standard streams so that the output can be passed to listeners.
   */
  private static void redirectStreams(final Serializer serializer) {
    final Object lock = new Object();

    stdout = System.out;
    stderr = System.err;
    System.setOut(new PrintStream(new BufferedOutputStream(new ChunkedStream() {
      @Override
      public void write(byte[] b, int off, int len) throws IOException {
        synchronized (lock) {
          serializer.serialize(new AppendStdOutEvent(b, off, len));
        }
      }
    })));

    System.setErr(new PrintStream(new BufferedOutputStream(new ChunkedStream() {
      @Override
      public void write(byte[] b, int off, int len) throws IOException {
        synchronized (lock) {
          serializer.serialize(new AppendStdErrEvent(b, off, len));
        }
      }
    })));
  }

  /**
   * Flush current streams and restore original ones.
   */
  private static void restoreStreams() {
    if (stdout != null) {
      System.out.flush();
      System.setOut(stdout);
    }

    if (stderr != null) {
      System.err.flush();
      System.setErr(stderr);
    }
  }
  

  /**
   * Warning emitter. Uses whatever alternative non-event communication channel is.
   */
  private static void warn(String string, Throwable t) {
    if (warnings == null) return;

    warnings.println("WARN: " + string);
    if (t != null) {
      warnings.println("      " + t.toString());
      t.printStackTrace(stderr);
    }
    warnings.flush();
  }
}
