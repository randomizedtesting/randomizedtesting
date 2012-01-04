#!/bin/bash

CFLAGS="-I ${JAVA_HOME}/include -I ${JAVA_HOME}/include/linux"

# Linux, 64-bit.
gcc -m64 ${CFLAGS} -fPIC -c crash.c
ld -shared -static -o lib/libcrash64.so *.o

# Linux, 32-bit.
gcc -m32 ${CFLAGS} -fPIC -c crash.c
ld -melf_i386 -shared -static -o lib/libcrash.so *.o

# Windows, 32-bit
rm *.o
i686-w64-mingw32-gcc ${CFLAGS} -fPIC -c crash.c
i686-w64-mingw32-ld -shared -static -o lib/crash.dll *.o

# Windows, 64-bit
rm *.o
x86_64-w64-mingw32-gcc ${CFLAGS} -fPIC -c crash.c
x86_64-w64-mingw32-ld -shared -static -o lib/crash64.dll *.o

rm *.o