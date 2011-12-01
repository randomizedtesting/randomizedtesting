
-dontoptimize

-dontnote
-dontwarn

-renamepackage com.google=>com.carrotsearch.ant.tasks.junit4.dependencies
-renamepackage org.objectweb=>com.carrotsearch.ant.tasks.junit4.dependencies
-renamepackage org.apache=>com.carrotsearch.ant.tasks.junit4.dependencies
-renamepackage org.simpleframework=>com.carrotsearch.ant.tasks.junit4.dependencies
-renamepackage com.carrotsearch.randomizedtesting=>com.carrotsearch.ant.tasks.junit4.dependencies
-repackageclasses com.carrotsearch.ant.tasks.junit4.dependencies

-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*

-keep class com.carrotsearch.ant.** {
    <methods>; <fields>;
}

-keep class org.simpleframework.** {
    <methods>; <fields>;
}