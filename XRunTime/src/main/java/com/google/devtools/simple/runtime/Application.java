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
import com.google.devtools.simple.runtime.components.Form;
import com.google.devtools.simple.runtime.variants.Variant;

/**
 * Implementation of various application related runtime functions.
 * 
 * @author Herbert Czymontek
 */
@SimpleObject
public abstract class Application {

  private static ApplicationFunctions applicationFunctions;

  private Application() {
  }

  /**
   * Initializes the application functionality of the application library.
   *
   * @param functions  implementation of application functions
   */
  public static void initialize(ApplicationFunctions functions) {
    applicationFunctions = functions;
  }

  /**
   * Creates a new menu item with the given caption.
   *
   * <p>The caption will also be used to identify the menu item in the menu
   * event handler.
   *
   * @param caption  menu item caption
   */
  @SimpleFunction
  public static void AddMenuItem(String caption) {
    applicationFunctions.addMenuItem(caption);
  }

  /**
   * Display a different form.
   *
   * @param form  form to display
   */
  @SimpleFunction
  public static void SwitchForm(Form form) {
    applicationFunctions.switchForm(form);
  }

  /**
   * Terminates this application.
   */
  @SimpleFunction
  public static void Finish() {
    applicationFunctions.finish();
  }

  /**
   * Retrieves the value of a previously stored preference (even from previous
   * of the same program).
   *
   * @param name  name which was used to store the value under
   * @return  value associated with name
   */
  @SimpleFunction
  public static Variant GetPreference(String name) {
    return applicationFunctions.getPreference(name);
  }

  /**
   * Stores the given value under given name. The value can be retrieved using
   * the given name any time (even on subsequent runs of the program).
   * 
   * @param name  name to store value under
   * @param value  value to store (must be a primitive value, objects not
   *               allowed)
   */
  @SimpleFunction
  public static void StorePreference(String name, Variant value) {
    applicationFunctions.storePreference(name, value);
  }
}
