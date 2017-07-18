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

/**
 * Interface for logging related functions.
 *
 * <p>Not accessible to Simple programmers.
 * 
 * @author Herbert Czymontek
 */
public interface LogFunctions {

  /**
   * Logs an error message.
   *
   * @param moduleName  name of the module reporting the message (e.g. "Simple
   *                    Runtime Library")
   * @param message  text to log
   */
  void error(String moduleName, String message);

  /**
   * Logs an warning message.
   *
   * @param moduleName  name of the module reporting the message (e.g. "Simple
   *                    Runtime Library")
   * @param message  text to log
   */
  void warning(String moduleName, String message);

  /**
   * Logs an info message.
   *
   * @param moduleName  name of the module reporting the message (e.g. "Simple
   *                    Runtime Library")
   * @param message  text to log
   */
  void info(String moduleName, String message);
}
