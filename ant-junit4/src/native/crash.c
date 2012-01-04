
#include "com_carrotsearch_ant_tasks_junit4_tests_Crash.h"
#include "stdio.h"

JNIEXPORT void JNICALL Java_com_carrotsearch_ant_tasks_junit4_tests_Crash_crashMe
  (JNIEnv * env, jclass clazz)
{
  ((char*) 0)[0] = 0;
}
