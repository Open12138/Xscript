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


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for command execution and I/O redirection.
 *
 * @author Herbert Czymontek
 */
public final class Execution {

  // Logging support
  private static final Logger LOG = Logger.getLogger(Execution.class.getName());

  /*
   * Input stream handler used for stdout and stderr redirection.
   */
  private static class RedirectStreamHandler extends Thread {
    // Streams to redirect from and to
    private final InputStream input;
    private final PrintWriter output;

    RedirectStreamHandler(PrintWriter output, InputStream input) {
      this.input = Preconditions.checkNotNull(input);
      this.output = Preconditions.checkNotNull(output);
      start();
    }

    @Override
    public void run() {
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        while ((line = reader.readLine()) != null) {
            output.println(line);
        }
      } catch (IOException ioe) {
        // OK to ignore...
        LOG.log(Level.WARNING, "____I/O Redirection failure: ", ioe);
      }
    }
  }

  private Execution() {
  }

  /**
   * Combines the contents of two string lists into a single string array.
   *
   * @param list1  first list of strings
   * @param list2  second list of strings
   * @return  combined string array
   */
  public static String[] combineIntoStringArray(List<String> list1, List<String> list2) {
    List<String> combinedList = new ArrayList<String>();
    combinedList.addAll(list1);
    combinedList.addAll(list2);
    return combinedList.toArray(new String[combinedList.size()]);
  }

  /**
   * Executes a command in a command shell.
   *
   * @param workingDir  working directory for the command
   * @param command  command to execute and its arguments
   * @param out  standard output stream to redirect to
   * @param err  standard error stream to redirect to
   * @return  {@code true} if the command succeeds, {@code false} otherwise
   */
  public static boolean execute(File workingDir, String[] command, PrintStream out,
      PrintStream err) {
    LOG.log(Level.INFO, "____Executing " + Strings.join(" ", command));

    try {
      Process process = Runtime.getRuntime().exec(command, null, workingDir);
      new RedirectStreamHandler(new PrintWriter(out, true), process.getInputStream());
      new RedirectStreamHandler(new PrintWriter(err, true), process.getErrorStream());
      return process.waitFor() == 0;
    } catch (Exception e) {
      LOG.log(Level.WARNING, "____Execution failure: ", e);
      return false;
    }
  }
}
