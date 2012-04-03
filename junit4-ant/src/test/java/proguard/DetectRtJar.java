package proguard;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.io.Files;


public class DetectRtJar {
  public static void main(String[] args) throws Exception {
    Set<File> jars = new TreeSet<File>();

    Class<?> [] keyClasses = {
        java.lang.annotation.Annotation.class,
        java.lang.management.ManagementFactory.class,
        java.util.logging.Logger.class,
        java.awt.Component.class,
        java.beans.BeanDescriptor.class,
        java.io.File.class,
        java.lang.Object.class,
        java.math.BigDecimal.class,
        java.net.URL.class,
        java.nio.Buffer.class,
        java.security.Security.class,
        java.sql.Array.class,
        java.text.Collator.class,
        java.util.List.class,
        java.util.concurrent.ConcurrentHashMap.class,
        java.util.zip.ZipEntry.class,
        org.w3c.dom.Document.class,
    };

    ClassLoader cl = ClassLoader.getSystemClassLoader();
    for (Class<?> clazz : keyClasses) {
      URL url = cl.getResource(
          clazz.getName().replace('.', '/') + ".class");
      if (url.getProtocol().equals("jar")) {
        JarURLConnection juc = (JarURLConnection) url.openConnection();
        jars.add(new File(juc.getJarFile().getName()));
      } else {
        // Other scheme? wtf?
        throw new RuntimeException("Unknown scheme: " + url.toString());
      }
    }

    StringBuilder b = new StringBuilder();
    for (File f : jars) {
      b.append("-libraryjar ").append(f.getAbsolutePath());
      b.append("(java/**)");
      b.append("\n");
    }

    System.out.println("Dumping rt.jar path to: "  + args[0]);
    Files.write(
        b.toString(), new File(args[0]), Charset.defaultCharset());
  }
}
