package com.carrotsearch.ant.tasks.junit4;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;


public class Demo {
  public static void main(String[] args) throws Exception {
    // Spawn the child process.
    ProcessBuilder pb = new ProcessBuilder()
      .command(
        "java", 
        "-cp", System.getProperty("java.class.path"),
        SlaveMain.class.getName());
    Process process = pb.start();
    InputStream stdout = process.getInputStream();
    InputStream stderr = process.getErrorStream();

    IExecutionListener proxy = runListenerProxy(new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args)
          throws Throwable {
        if (method.getName().startsWith("append")) {
          System.out.write((byte[]) args[0]);
        } else {
          System.out.println(method.getName() + " " + Arrays.toString(args));
        }
        return null;
      }
    });

    EventReader replay = new EventReader(stdout, proxy, IExecutionListener.class);
    replay.replay();

    int status = process.waitFor();
    System.out.println("Finished: " + status);
  }
  
  /**
   * Creates a proxy for {@link IExecutionListener} to a given handler.
   */
  private static IExecutionListener runListenerProxy(InvocationHandler handler) {
    return (IExecutionListener) Proxy.newProxyInstance(Thread.currentThread()
        .getContextClassLoader(), new Class<?>[] {IExecutionListener.class},                                                 handler);
  }  
}
