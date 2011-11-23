
-dontnote
-dontoptimize
-dontwarn

-renamepackage com.google=>com.carrotsearch.ant.tasks.junit4.dependencies
-renamepackage org.objectweb=>com.carrotsearch.ant.tasks.junit4.dependencies
-repackageclasses com.carrotsearch.ant.tasks.junit4.dependencies

-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*

-keep class com.carrotsearch.** {
    <methods>; <fields>;
}

-dontnote
-libraryjars <java.home>/lib/rt.jar(java/**)
