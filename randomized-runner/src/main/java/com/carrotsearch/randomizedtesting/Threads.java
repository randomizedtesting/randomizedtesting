package com.carrotsearch.randomizedtesting;

import java.lang.Thread.State;
import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;

final class Threads {
  private Threads() {}
  
  /**
   * Collect thread information, JVM vendor insensitive.
   */
  public static String threadName(Thread t) {
    return "Thread[" +
        "id=" + t.getId() +
        ", name=" + t.getName() +
        ", state=" + t.getState() +
        ", group=" + groupName(t.getThreadGroup()) +
        "]";
  }

  private static String groupName(ThreadGroup threadGroup) {
    if (threadGroup == null) {
      return "{null group}";
    } else {
      return threadGroup.getName();
    }
  }

  private final static EnumMap<State,String> lockInfoStrings;
  static {
    lockInfoStrings = new EnumMap<State,String>(State.class);
    lockInfoStrings.put(State.BLOCKED, "blocked on ");
    lockInfoStrings.put(State.WAITING, "waiting on ");
    lockInfoStrings.put(State.TIMED_WAITING, "timed waiting on ");
    lockInfoStrings.put(State.TERMINATED, "terminated? on ");
    lockInfoStrings.put(State.RUNNABLE, "runnable? on ");
    lockInfoStrings.put(State.NEW, "new? on ");
  }
  
  /**
   * Dump {@link ThreadInfo} information.
   */
  public static void append(StringBuilder b, ThreadInfo ti) {
    b.append('"').append(ti.getThreadName()).append('"');
    b.append(" ID=").append(ti.getThreadId());

    final State threadState = ti.getThreadState();
    b.append(" ").append(threadState);
    if (ti.getLockName() != null) {
      b.append(" on ").append(ti.getLockName());
    }
    
    if (ti.getLockOwnerName() != null) {
      b.append(" owned by \"").append(ti.getLockOwnerName())
       .append("\" ID=").append(ti.getLockOwnerId());
    }
    
    b.append(ti.isSuspended() ? " (suspended)" : "");
    b.append(ti.isInNative() ? " (in native code)" : "");
    b.append("\n");
    
    final StackTraceElement[] stack = ti.getStackTrace();
    final LockInfo lockInfo = ti.getLockInfo();
    final MonitorInfo [] monitorInfos = ti.getLockedMonitors();
    for (int i = 0; i < stack.length; i++) {
      b.append("\tat ").append(stack[i]).append("\n");
      if (i == 0 && lockInfo != null) {
        b.append("\t- ")
         .append(lockInfoStrings.get(threadState))
         .append(lockInfo)
         .append("\n");
      }
      
      for (MonitorInfo mi : monitorInfos) {
        if (mi.getLockedStackDepth() == i) {
          b.append("\t- locked ").append(mi).append("\n");
        }
      }
    }

    LockInfo [] lockInfos = ti.getLockedSynchronizers();
    if (lockInfos.length > 0) {
      b.append("\tLocked synchronizers:\n");
      for (LockInfo li : ti.getLockedSynchronizers()) {
        b.append("\t- ").append(li).append("\n");
      }
    }
    b.append("\n");
  }

  public static HashSet<Thread> getAllThreads() {
    ThreadGroup tg = getTopThreadGroup();
    return getThreads(tg);
  }

  public static HashSet<Thread> getThreads(ThreadGroup tg) {
    Thread [] threads = new Thread [2];
    int maxIndex;
    while ((maxIndex = doEnumerate(tg, threads, true)) == threads.length) {
      threads = new Thread [threads.length * 2];
    }
    return new HashSet<Thread>(Arrays.asList(threads).subList(0, maxIndex));
  }
  
  private static int doEnumerate(final ThreadGroup tg, final Thread[] threads, final boolean recurse) {
    return AccessController.doPrivileged(new PrivilegedAction<Integer>() {
      @Override
      public Integer run() {
        return tg.enumerate(threads, recurse);
      }
    });
  }

  public static ThreadGroup getTopThreadGroup() {
    // a lame workaround so that J9 works.
    ThreadGroup tg = AccessController.doPrivileged(new PrivilegedAction<ThreadGroup>() {
      @Override
      public ThreadGroup run() {
        ThreadGroup tg = Thread.currentThread().getThreadGroup(); 
        while (tg != null && tg.getParent() != null) {
          tg = tg.getParent();
        }
        return tg;
      }
    });

    if (tg == null) {
      throw new RuntimeException("No root ThreadGroup for thread: " + Thread.currentThread());
    }
    return tg;
  }
}
