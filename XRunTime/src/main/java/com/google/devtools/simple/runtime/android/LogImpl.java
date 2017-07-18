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

package com.google.devtools.simple.runtime.android;

import com.google.devtools.simple.runtime.LogFunctions;

import android.content.Context;
import android.util.Log;

/**
 * Implementation of logging related functions.
 * 
 * @author Herbert Czymontek
 */
public final class LogImpl implements LogFunctions {

  /**
   * Creates and initializes new phone functions implementation
   *
   * @param context  activity context
   */
  public LogImpl(Context context) {
    // We don't really need the log context (so far), but it is nice to have symmetrical APIs
  }

  // LogFunctions implementation

  @Override
  public void error(String moduleName, String message) {
    Log.e(moduleName, message);
  }

  @Override
  public void info(String moduleName, String message) {
    Log.i(moduleName, message);
  }

  @Override
  public void warning(String moduleName, String message) {
    Log.w(moduleName, message);
  }
}
