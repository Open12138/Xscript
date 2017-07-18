/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.simple.util;

import android.annotation.TargetApi;
import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Helper methods to deal with files.
 *
 * @author Herbert Czymontek
 */
public final class Files {

    private Files() {
    }

    /**
     * Creates a new directory (if it doesn't exist already).
     *
     * @param dir new directory
     * @return new directory
     */
    public static File createDirectory(File dir) {
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    /**
     * Creates a new directory (if it doesn't exist already).
     *
     * @param parentDirectory parent directory of new directory
     * @param name            name of new directory
     * @return new directory
     */
    public static File createDirectory(File parentDirectory, String name) {
        File dir = new File(parentDirectory, name);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    /**
     * Reads a file into a string.
     *
     * @param file     file to read from
     * @param encoding character encoding of file
     * @return file contents in string form
     * @throws IOException if the file couldn't be read for any reason
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static String read(File file, String encoding) throws IOException {
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            throw new IOException("File to large - " + file.getName());
        }

        InputStream is = new FileInputStream(file);
        byte[] data = new byte[(int) length];

        try {
            if (is.read(data) != data.length) {
                throw new IOException("Cannot read file - " + file.getName());
            }
        } finally {
            is.close();
        }

        return new String(data, Charset.forName(encoding));
    }

    /**
     * Writes the given data into a file.
     *
     * @param data data to write
     * @param file file to write to
     * @throws IOException if the file couldn't be written for any reason
     */
    public static void write(byte[] data, File file) throws IOException {
        OutputStream os = new FileOutputStream(file);
        try {
            os.write(data);
        } finally {
            os.close();
        }
    }

    /*
     * Recursively visits source directories and adds found source files to the list of source
     * files.
     */
    private static void visitDirectories(List<String> files, String root, File file, String regex) {
        if (file.isDirectory()) {
            // Recursively visit nested directories.
            for (String child : file.list()) {
                visitDirectories(files, root, new File(file, child), regex);
            }
        } else if (file.getName().matches(regex)) {
            files.add(file.getAbsolutePath().substring(root.length() + 1));
        }
    }

    /**
     * Finds files whose names are matching a regular expression in a list of
     * subdirectories of a given root directory.
     *
     * @param files   list to add matching files to
     * @param root    root directory
     * @param dirList list of comma separated subdirectories
     * @param regex   regular expression for file names to match (not wildcards!)
     */
    public static void findFiles(List<String> files, File root, String dirList, String regex) {
        String rootPath = root.getAbsolutePath();
        for (String dir : dirList.split(",")) {
            visitDirectories(files, rootPath, new File(root, dir), regex);
        }
    }
}
