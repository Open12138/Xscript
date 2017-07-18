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

package com.google.devtools.simple.runtime;

import com.google.devtools.simple.runtime.annotations.SimpleFunction;
import com.google.devtools.simple.runtime.annotations.SimpleObject;

/**
 * Implementation of logging related runtime functions.
 * 
 * @author Herbert Czymontek
 */
@SimpleObject
public final class Log {

  // Module name for Simple runtime library
  public static final String MODULE_NAME_RTL = "Simple Runtime Library";
  
  private static LogFunctions logFunctions;

  private Log() {
  }

  /**
   * Initializes the logging.
   *
   * @param functions  implementation of logging functions
   */
  public static void initialize(LogFunctions functions) {
    logFunctions = functions;
  }

  /**
   * Logs an error message.
   *
   * @param moduleName  name of the module reporting the message (e.g. "Simple
   *                    Runtime Library")
   * @param message  text to log
   */
  @SimpleFunction
  public static void Error(String moduleName, String message) {
    logFunctions.error(moduleName, message);
  }

  /**
   * Logs an warning message.
   *
   * @param moduleName  name of the module reporting the message (e.g. "Simple
   *                    Runtime Library")
   * @param message  text to log
   */
  @SimpleFunction
  public static void Warning(String moduleName, String message) {
    logFunctions.warning(moduleName, message);
  }

  /**
   * Logs an info message.
   *
   * @param moduleName  name of the module reporting the message (e.g. "Simple
   *                    Runtime Library")
   * @param message  text to log
   */
  @SimpleFunction
  public static void Info(String moduleName, String message) {
    logFunctions.info(moduleName, message);
  }
}
