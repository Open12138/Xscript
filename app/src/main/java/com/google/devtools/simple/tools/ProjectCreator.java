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

package com.google.devtools.simple.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;

import com.google.devtools.simple.compiler.util.Signatures;

/**
 * Creates a new Simple project.
 *
 * @author Herbert Czymontek
 */
public class ProjectCreator {

  private static void usage() {
    System.err.println("Usage: newsimpleproject qualified-form-name\n" +
                       "           e.g. simpleproject com.yourdomain.Test");
    System.exit(1);
  }

  private static void fatal(String msg) {
    System.err.println("Error: " + msg);
    System.exit(-1);
  }

  private static void createDirectories(File dir) throws IOException {
    if (!dir.mkdirs()) {
      throw new IOException("cannot create directories " + dir);
    }
  }

  private static void createTextFile(File dir, String name, String content) throws IOException {
    File file = new File(dir, name);
    file.createNewFile();
    Writer output = new BufferedWriter(new FileWriter(file));
    try {
      output.write(content);
    } finally {
      output.close();
    }    
  }

  /**
   * Main entry point.
   *
   * @param args  command line arguments
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      usage();
    }

    // Check qualified form name
    String qualifiedFormName = args[0];
    if (!Pattern.matches("^(([a-z])+.)+[A-Z]([A-Za-z])+$", qualifiedFormName)) {
      fatal("malformed qualified form name - must be a valid Java class name, " +
      		"e.g. com.yourdomain.Test");
    }

    // Get form name components
    String formName = Signatures.getClassName(qualifiedFormName);
    String packageName = Signatures.getPackageName(qualifiedFormName);

    // Create project root directory with simpleproject directory and src directory
    File projectDir = new File(formName + "/simpleproject");
    File srcDir = new File(formName + "/src/" + packageName.replace('.', '/'));
    File assetsDir = new File(formName + "/assets");
    try {
      createDirectories(projectDir);
      createDirectories(srcDir);
      createDirectories(assetsDir);

    } catch (IOException e) {
      fatal("Cannot create directories");
    }

    try {
      // Create project file
      createTextFile(projectDir, "project.properties",
          "main=" + qualifiedFormName + "\n" +
          "name=" + formName + "\n" +
          "assets=../assets\n" +
          "source=../src\n" +
          "build=../build\n");

      // Create Form source file
      createTextFile(srcDir, formName + ".simple",
          "$Properties\n" +
          "$Source $Form\n" +
          "$Define " + formName + " $As Form\n" +
          "Layout = 1\n" +
          "Layout.Orientation = 1\n" +
          "$End $Define\n" +
          "$End $Properties\n");

    } catch (IOException e) {
      fatal("Cannot create source file");
    }
  }
}
