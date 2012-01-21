#!/bin/bash

CFLAGS="-I ${JAVA_HOME}/include -I ${JAVA_HOME}/include/linux -I ${JAVA_HOME}/include/win32"

# Linux, 64-bit.
gcc -m64 ${CFLAGS} -fPIC -c crash.c
ld -shared -static -o lib/libcrash64.so *.o

# Linux, 32-bit.
gcc -m32 ${CFLAGS} -fPIC -c crash.c
ld -melf_i386 -shared -static -o lib/libcrash.so *.o

# Windows, 32-bit
rm -f *.o
i686-w64-mingw32-gcc ${CFLAGS} -shared -o lib/crash.dll crash.c

# Windows, 64-bit
rm -f *.o
x86_64-w64-mingw32-gcc ${CFLAGS} -shared -o lib/crash64.dll crash.c 

rm -f *.o
