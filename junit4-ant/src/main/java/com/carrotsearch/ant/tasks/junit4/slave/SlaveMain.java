package com.carrotsearch.ant.tasks.junit4.slave;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import com.carrotsearch.ant.tasks.junit4.events.AppendStdErrEvent;
import com.carrotsearch.ant.tasks.junit4.events.AppendStdOutEvent;
import com.carrotsearch.ant.tasks.junit4.events.BootstrapEvent;
import com.carrotsearch.ant.tasks.junit4.events.Serializer;
import com.carrotsearch.ant.tasks.junit4.events.SuiteFailureEvent;
import com.carrotsearch.randomizedtesting.MethodGlobFilter;
import com.carrotsearch.randomizedtesting.SysGlobals;
import com.carrotsearch.randomizedtesting.annotations.SuppressForbidden;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;

/**
 * A slave process running the actual tests on the target JVM.
 */
public class SlaveMain {
  /** Runtime exception. */
  public static final int ERR_EXCEPTION = 240;

  /** No JUnit on classpath. */
  public static final int ERR_NO_JUNIT = 239;

  /** Old JUnit on classpath. */
  public static final int ERR_OLD_JUNIT = 238;

  /** OOM */
  public static final int ERR_OOM = 237;

  /**
   * Last resort memory pool released under low memory conditions.
   * This is not a solution, it's a terrible hack. I know this. Everyone knows this.
   * Even monkeys in Madagaskar know this. If you know a better solution, patches
   * welcome. 
   * 
   * <p>Approximately 5mb is reserved. Really, smaller values don't make any difference
   * and the JVM fails to even return the status passed to Runtime.halt().
   */
  static volatile Object lastResortMemory = new byte [1024 * 1024 * 5];

  /**
   * Preallocate and load in advance. 
   */
  static Class<OutOfMemoryError> oomClass = OutOfMemoryError.class;
  
  /**
   * Frequent event strean flushing.
   */
  public static final String OPTION_FREQUENT_FLUSH = "-flush";

  /**
   * Multiplex sysout and syserr to original streams (aside from
   * pumping them to event stream).
   */
  public static final String OPTION_SYSOUTS = "-sysouts";

  /**
   * Read class names from standard input.
   */
  public static final String OPTION_STDIN = "-stdin";

  /**
   * Name the sink for events. If given, accepts one argument - name of a file
   * to which events should be dumped. The file has to be initially empty!
   */
  public static final String OPTION_EVENTSFILE = "-eventsfile";

  /**
   * Should the debug stream from the runner be created? It's named after the events file
   * with <code>.debug</code> suffix.
   */
  public static final String OPTION_DEBUGSTREAM = "-debug";

  /**
   * User-defined RunListener classes.
   */
  public static final String OPTION_RUN_LISTENERS = "-runListeners";

  /**
   * Fire a runner failure after startup to verify messages
   * are propagated properly. Not really useful in practice...
   */
  public static final String SYSPROP_FIRERUNNERFAILURE =
      SlaveMain.class.getName() + ".fireRunnerFailure";

  /**
   * Delay the initial bootstrap event from the forked JVM 
   * (used in tests).
   */
  public static final String SYSPROP_FORKEDJVM_DELAY_MS =
      "junit4.tests.internal.initialDelayMs";

  /**
   * Event sink.
   */
  private final Serializer serializer;

  /** A sink for warnings (non-event stream). */
  private static PrintStream warnings;

  /** Flush serialization stream frequently. */
  private boolean flushFrequently = false;

  /** Debug stream to flush progress information to. */
  private File debugMessagesFile;

  /** List of RunListener classes */
  private String runListeners;

  /** 
   * Multiplex calls to System streams to both event stream
   * and the original streams?
   */
  private static boolean multiplexStdStreams = false;

  /**
   * Base for redirected streams. 
   */
  private static class ChunkedStream extends OutputStream {
    public void write(int b) throws IOException {
      throw new IOException("Only buffered write(byte[],int,int) calls expected from super stream.");
    }
    
    @Override
    public void close() throws IOException {
      throw new IOException("Not supposed to be called on redirected streams.");
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
  private void execute(Iterator<String> classNames) throws Throwable {
    final RunNotifier fNotifier = new OrderedRunNotifier();
    final Result result = new Result();
    final Writer debug = debugMessagesFile == null ? new NullWriter() : new OutputStreamWriter(new FileOutputStream(debugMessagesFile), "UTF-8");

    fNotifier.addListener(result.createListener());

    fNotifier.addListener(
        new StreamFlusherDecorator(
            new NoExceptionRunListenerDecorator(new RunListenerEmitter(serializer)) {
              @Override
              protected void exception(Throwable t) {
                warn("Event serializer exception.", t);
              }
            }));

    fNotifier.addListener(new RunListener() {
      public void testRunFinished(Result result) throws Exception {
        debug(debug, "testRunFinished(T:" + result.getRunCount() + ";F:" + result.getFailureCount() + ";I:" + result.getIgnoreCount() + ")");
        serializer.flush();
      }

      @Override
      public void testRunStarted(Description description) throws Exception {
        debug(debug, "testRunStarted(" + description + ")");
        serializer.flush();
      }
      
      @Override
      public void testStarted(Description description) throws Exception {
        debug(debug, "testStarted(" + description + ")");
        serializer.flush();
      }
      
      public void testFinished(Description description) throws Exception {
        debug(debug, "testFinished(" + description + ")");
        serializer.flush();
      }
      
      @Override
      public void testIgnored(Description description) throws Exception {
        debug(debug, "testIgnored(T:" + description + ")");
      }
      
      @Override
      public void testFailure(Failure failure) throws Exception {
        debug(debug, "testFailure(T:" + failure + ")");
      }
      
      @Override
      public void testAssumptionFailure(Failure failure) {
        try {
          debug(debug, "testAssumptionFailure(T:" + failure + ")");
        } catch (IOException e) {
          throw new RuntimeException(e);
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
    debug(debug, "Entering main suite loop.");
    try {
      while (classNames.hasNext()) {
        final String clName = classNames.next();
        debug(debug, "Instantiating: " + clName);
        Class<?> clazz = instantiate(clName);
        if (clazz == null) 
          continue;
  
        Request request = Request.aClass(clazz);
        try {
          Runner runner = request.getRunner();
          methodFilter.apply(runner);

          // New RunListener instances should be added per class and then removed from the RunNotifier
          ArrayList<RunListener> runListenerInstances = instantiateRunListeners();
          for (RunListener runListener : runListenerInstances) {
            fNotifier.addListener(runListener);
          }

          fNotifier.fireTestRunStarted(runner.getDescription());
          debug(debug, "Runner.run(" + clName + ")");
          runner.run(fNotifier);
          debug(debug, "Runner.done(" + clName + ")");
          fNotifier.fireTestRunFinished(result);

          for (RunListener runListener : runListenerInstances) {
            fNotifier.removeListener(runListener);
          }
        } catch (NoTestsRemainException e) {
          // Don't complain if all methods have been filtered out. 
          // I don't understand the reason why this exception has been
          // built in to filters at all.
        }
      }
    } catch (Throwable t) {
      debug(debug, "Main suite loop error: " + t);
      throw t;
    } finally {
      debug(debug, "Leaving main suite loop.");
      debug.close();
    }
  }

  private void debug(Writer w, String msg) throws IOException {
    w.write(msg);
    w.write("\n");
    w.flush();
  }

  /**
   * Instantiate test classes (or try to).
   */
  private Class<?> instantiate(String className) {
    try {
      return Class.forName(className, false, Thread.currentThread().getContextClassLoader());
    } catch (Throwable t) {
      try {
        serializer.serialize(
            new SuiteFailureEvent(
                new Failure(Description.createSuiteDescription(className), t)));
        if (flushFrequently)
          serializer.flush();
      } catch (Exception e) {
        warn("Could not report failure back to master.", t);
      }
      return null;
    }
  }

  /**
   * Console entry point.
   */
  @SuppressWarnings("resource")
  public static void main(String[] allArgs) {
    int exitStatus = 0;

    Serializer serializer = null;
    try {
      final ArrayDeque<String> args = new ArrayDeque<String>(Arrays.asList(allArgs));

      // Options.
      boolean debugStream = false;
      boolean flushFrequently = false;
      File  eventsFile = null;
      boolean suitesOnStdin = false;
      List<String> testClasses = new ArrayList<>();
      String runListeners = null;

      while (!args.isEmpty()) {
        String option = args.pop();
        if (option.equals(OPTION_FREQUENT_FLUSH)) {
          flushFrequently = true;
        } else if (option.equals(OPTION_STDIN)) {
          suitesOnStdin = true;
        } else if (option.equals(OPTION_SYSOUTS)) {
          multiplexStdStreams = true;
        } else if (option.equals(OPTION_EVENTSFILE)) {
          eventsFile = new File(args.pop());
          if (eventsFile.isFile() && eventsFile.length() > 0) {
            RandomAccessFile raf = new RandomAccessFile(eventsFile, "rw");
            raf.setLength(0);
            raf.close();
          }
        } else if (option.equals(OPTION_RUN_LISTENERS)) {
          runListeners = args.pop();
        } else if (option.startsWith(OPTION_DEBUGSTREAM)) {
          debugStream = true;
        } else if (option.startsWith("@")) {
          // Append arguments file, one line per option.
          args.addAll(Arrays.asList(readArgsFile(option.substring(1))));
        } else {
          // The default expectation is a test class.
          testClasses.add(option);
        }
      }

      // Set up events channel and events serializer.
      if (eventsFile == null) {
        throw new IOException("You must specify communication channel for events.");
      }

      // Delay the forked JVM a bit (for tests).
      if (System.getProperty(SYSPROP_FORKEDJVM_DELAY_MS) != null) {
        Thread.sleep(Integer.parseInt(System.getProperty(SYSPROP_FORKEDJVM_DELAY_MS)));
      }
      
      // Send bootstrap package.
      serializer = new Serializer(new EventsOutputStream(eventsFile))
        .serialize(new BootstrapEvent())
        .flush();

      // Redirect original streams and start running tests.
      redirectStreams(serializer, flushFrequently);

      final SlaveMain main = new SlaveMain(serializer);
      main.flushFrequently = flushFrequently;
      main.debugMessagesFile = debugStream ? new File(eventsFile.getAbsolutePath() + ".debug"): null;
      main.runListeners = runListeners;

      final Iterator<String> stdInput;
      if (suitesOnStdin) { 
        stdInput = new StdInLineIterator(main.serializer);
      } else {
        stdInput = Collections.<String>emptyList().iterator();
      }

      main.execute(Iterators.concat(testClasses.iterator(), stdInput));
      
      // For unhandled exceptions tests.
      if (System.getProperty(SYSPROP_FIRERUNNERFAILURE) != null) {
        throw new Exception(System.getProperty(SYSPROP_FIRERUNNERFAILURE));
      }
    } catch (Throwable t) {
      lastResortMemory = null;
      tryWaitingForGC();

      if (t.getClass() == oomClass) {
        exitStatus = ERR_OOM;
        warn("JVM out of memory.", t);
      } else {
        exitStatus = ERR_EXCEPTION;
        warn("Exception at main loop level.", t);
      }
    }

    try {
      if (serializer != null) {
        try {
          serializer.close();
        } catch (Throwable t) {
          warn("Exception closing serializer.", t);
        }
      }
    } finally {
      JvmExit.halt(exitStatus);
    }
  }

  /**
   * Try waiting for a GC to happen. This is a dirty heuristic but if we're
   * here we're neck deep in sh*t anyway (OOMs all over).
   */
  private static void tryWaitingForGC() {
    // We could try to preallocate memory mx bean and count collections...
    // there is no guarantee it doesn't allocate stuff too though.
    final long timeout = System.currentTimeMillis() + 2000;
    while (System.currentTimeMillis() < timeout) {
      System.gc(); 
      try {
        Thread.sleep(250);
      } catch (InterruptedException e) {
        break;
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
   * Redirect standard streams so that the output can be passed to listeners.
   */
  @SuppressForbidden("legitimate sysstreams.")
  private static void redirectStreams(final Serializer serializer, final boolean flushFrequently) {
    final PrintStream origSysOut = System.out;
    final PrintStream origSysErr = System.err;

    // Set warnings stream to System.err.
    warnings = System.err;
    AccessController.doPrivileged(new PrivilegedAction<Void>() {
      @SuppressForbidden("legitimate PrintStream with default charset.")
      @Override
      public Void run() {
        System.setOut(new PrintStream(new BufferedOutputStream(new ChunkedStream() {
          @Override
          public void write(byte[] b, int off, int len) throws IOException {
            if (multiplexStdStreams) {
              origSysOut.write(b, off, len);
            }
            serializer.serialize(new AppendStdOutEvent(b, off, len));
            if (flushFrequently) serializer.flush();
          }
        })));

        System.setErr(new PrintStream(new BufferedOutputStream(new ChunkedStream() {
          @Override
          public void write(byte[] b, int off, int len) throws IOException {
            if (multiplexStdStreams) {
              origSysErr.write(b, off, len);
            }
            serializer.serialize(new AppendStdErrEvent(b, off, len));
            if (flushFrequently) serializer.flush();
          }
        })));
        return null;
      }
    });
  }

  /**
   * Warning emitter. Uses whatever alternative non-event communication channel is.
   */
  @SuppressForbidden("legitimate sysstreams.")
  public static void warn(String message, Throwable t) {
    PrintStream w = (warnings == null ? System.err : warnings);
    try {
      w.print("WARN: ");
      w.print(message);
      if (t != null) {
        w.print(" -> ");
        try {
          t.printStackTrace(w);
        } catch (OutOfMemoryError e) {
          // Ignore, OOM.
          w.print(t.getClass().getName());
          w.print(": ");
          w.print(t.getMessage());
          w.println(" (stack unavailable; OOM)");
        }
      } else {
        w.println();
      }
      w.flush();
    } catch (OutOfMemoryError t2) {
      w.println("ERROR: Couldn't even serialize a warning (out of memory).");
    } catch (Throwable t2) {
      // Can't do anything, really. Probably an OOM?
      w.println("ERROR: Couldn't even serialize a warning.");
    }
  }

  /**
   * Generates JUnit 4 RunListener instances for any user defined RunListeners
   */
  private ArrayList<RunListener> instantiateRunListeners() throws Exception {
    ArrayList<RunListener> instances = new ArrayList<>();

    if (runListeners != null) {
      for (String className : Arrays.asList(runListeners.split(","))) {
        instances.add((RunListener) this.instantiate(className).newInstance());
      }
    }

    return instances;
  }
}
