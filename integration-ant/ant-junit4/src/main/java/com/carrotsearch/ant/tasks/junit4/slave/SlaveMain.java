package com.carrotsearch.ant.tasks.junit4.slave;

import java.io.*;
import java.util.*;

import org.apache.commons.io.output.TeeOutputStream;
import org.junit.runner.*;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import com.carrotsearch.ant.tasks.junit4.events.*;
import com.carrotsearch.ant.tasks.junit4.events.BootstrapEvent.EventChannelType;
import com.carrotsearch.randomizedtesting.MethodGlobFilter;
import com.carrotsearch.randomizedtesting.SysGlobals;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

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
   * Read class names from standard input.
   */
  public static final String OPTION_STDIN = "-stdin";
  
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
   * Sink log for the events stream.
   */
  private static File sinkLog;

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
  private void execute(Iterator<String> classNames) {
    final JUnitCore core = new JUnitCore();
    core.addListener(
        new StreamFlusherDecorator(
            new NoExceptionRunListenerDecorator(new RunListenerEmitter(serializer)) {
              @Override
              protected void exception(Throwable t) {
                warn("Event serializer exception.", t);
              }
            }));

    core.addListener(new RunListener() {
      public void testRunFinished(Result result) throws Exception {
        serializer.flush();
      }

      public void testFinished(Description description) throws Exception {
        if (flushFrequently) {
          serializer.flush();
        }
      }
    });

    /*
     * Instantiate method filter if any.
     */
    String methodFilterGlob = Strings.emptyToNull(System.getProperty(SysGlobals.SYSPROP_TESTMETHOD()));
    Filter methodFilter = Filter.ALL;
    if (methodFilterGlob != null) {
      methodFilter = new MethodGlobFilter(methodFilterGlob);
    }

    /*
     * Important. Run each class separately so that we get separate 
     * {@link RunListener} callbacks for the top extracted description.
     */
    while (classNames.hasNext()) {
      Class<?> clazz = instantiate(classNames.next());
      if (clazz == null) 
        continue;

      Request request = Request.aClass(clazz);
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
  private Class<?> instantiate(String className) {
    try {
      return Class.forName(className);
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
      return null;
    }
  }

  /**
   * Console entry point.
   */
  public static void main(String[] args) {
    if (System.getProperty("junit4.sinklog") != null) {
      sinkLog = new File(System.getProperty("junit4.sinklog"));
    }
    
    int exitStatus = 0;
    Serializer serializer = null; 
    try {
      // Pick the communication channel.
      final BootstrapEvent.EventChannelType channel = establishCommunicationChannel(); 
      new Serializer(System.out)
        .serialize(new BootstrapEvent(channel))
        .flush();

      OutputStream sink;
      final int bufferSize = 16 * 1024;
      switch (channel) {
        case STDERR:
          sink = System.err;
          warnings = System.out;
          break;

        case STDOUT:
          sink = System.out;
          warnings = System.err;
          break;

        default:
          warnings = System.err;
          throw new RuntimeException("Communication not implemented: " + channel);
      }

      if (sinkLog != null) {
        sink = new TeeOutputStream(sink, new FileOutputStream(sinkLog));
      }

      serializer = new Serializer(new BufferedOutputStream(sink, bufferSize));

      // Redirect original streams and start running tests.
      redirectStreams(serializer);
      final SlaveMain main = new SlaveMain(serializer);
      main.execute(parseArguments(main, args));
    } catch (Throwable t) {
      warn("Exception at main loop level?", t);
      exitStatus = ERR_EXCEPTION;
    } finally {
      restoreStreams();
    }

    if (serializer != null) {
      try {
        serializer.serialize(new QuitEvent());
        serializer.close();
      } catch (Throwable t) {
        warn("Exception closing serializer?", t);
        // Ignore.
      }
    }

    System.exit(exitStatus);
  }

  /**
   * Parse command line arguments and return an iterator
   * over test suites we should execute. The iterator may be
   * lazy (may block awaiting input) so tests should be executed as 
   * they are made available.
   */
  private static Iterator<String> parseArguments(SlaveMain main, String[] initial) throws IOException {
    ArrayDeque<String> args = new ArrayDeque<String>();
    args.addAll(Arrays.asList(initial));

    Iterator<String> stdInput = null; 
    List<String> testClasses = Lists.newArrayList();
    while (!args.isEmpty()) {
      String option = args.pop();
      if (option.equals(OPTION_FREQUENT_FLUSH)) {
        main.flushFrequently = true;
      } else if (option.equals(OPTION_STDIN)) {
        if (stdInput == null) {
          stdInput = new StdInLineIterator(main.serializer);
        }
      } else if (option.startsWith("@")) {
        // Append arguments file, one line per option.
        args.addAll(Arrays.asList(readArgsFile(option.substring(1))));
      } else {
        // The default expectation is a test class.
        testClasses.add(option);
      }
    }

    return Iterators.concat(
        testClasses.iterator(),
        stdInput != null ? stdInput : Collections.<String>emptyList().iterator());
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
    try {
      PrintStream w = (warnings == null ? System.err : warnings);
  
      w.println("WARN: " + string);
      if (t != null) {
        w.println("      " + t.toString());
        t.printStackTrace(w);
      }
      w.flush();
    } catch (Throwable t2) {
      // Can't do anything, really.
    }
  }
}
