#!/bin/bash

CFLAGS="-I /System/Library/Frameworks/JavaVM.framework/Headers/"
gcc -m64 ${CFLAGS}  -shared -static -fPIC crash.c -o lib/libcrash64.dylib
