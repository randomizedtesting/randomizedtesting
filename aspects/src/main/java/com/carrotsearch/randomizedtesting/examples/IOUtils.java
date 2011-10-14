package com.carrotsearch.randomizedtesting.examples;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class IOUtils
{
    public static String readFile(File file, Charset charset) throws IOException {
        byte [] bytes = new byte [(int) file.length()];
        DataInputStream is = new DataInputStream(new FileInputStream(file));
        is.readFully(bytes);
        is.close();
        return new String(bytes, charset);
    }
}
