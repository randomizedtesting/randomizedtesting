package com.carrotsearch.ant.tasks.junit4.slave;

import java.io.*;
import java.util.*;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;

import com.carrotsearch.ant.tasks.junit4.events.*;
import com.carrotsearch.ant.tasks.junit4.events.BootstrapEvent.EventChannelType;

/**
 * A slave process running the actual tests on the target JVM.
 */
public class SlaveMain {
  /** Runtime exception. */
  public static final int ERR_EXCEPTION = 255;

  /** No JUnit on classpath. */
  public static final int ERR_NO_JUNIT = 254;

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
    core.run(instantiate(classes));
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
      new Serializer(System.out).serialize(new BootstrapEvent(channel));

      switch (channel) {
        case STDERR:
          serializer = new Serializer(new BufferedOutputStream(System.err));
          warnings = System.out;
          break;

        case STDOUT:
          serializer = new Serializer(new BufferedOutputStream(System.out));
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

      // Emit QUIT and clean local serializer reference to avoid duplicate.
      Serializer dup = serializer;
      serializer = null;
      dup.serialize(new QuitEvent());
    } catch (Throwable t) {
      warn("Exception at main loop level?", t);
      exitStatus = -1;
    } finally {
      // Try hard to clean up the event stream.
      if (serializer != null) {
        try {
          serializer.serialize(new QuitEvent());
        } catch (IOException e) {
          // Ignore.
        }
      }
      restoreStreams();
    }

    if (serializer != null) {
      try {
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
      if (args[i].startsWith("@")) {
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
    stdout = System.out;
    stderr = System.err;
    System.setOut(new PrintStream(new BufferedOutputStream(new ChunkedStream() {
      @Override
      public synchronized void write(byte[] b, int off, int len) throws IOException {
        serializer.serialize(new AppendStdOutEvent(b, off, len));
      }
    })));

    System.setOut(new PrintStream(new BufferedOutputStream(new ChunkedStream() {
      @Override
      public synchronized void write(byte[] b, int off, int len) throws IOException {
        serializer.serialize(new AppendStdErrEvent(b, off, len));
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
