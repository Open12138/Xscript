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

import com.google.devtools.simple.runtime.components.Form;
import com.google.devtools.simple.runtime.variants.Variant;

/**
 * Interface for application related functions. Not accessible to Simple
 * programmers.
 * 
 * @author Herbert Czymontek
 */
public interface ApplicationFunctions {

  /**
   * Creates a new menu item with the given caption.
   *
   * <p>The caption will also be used to identify the menu item in the menu
   * event handler.
   *
   * @param caption  menu item caption
   */
  void addMenuItem(String caption);

  /**
   * Display a different form.
   *
   * @param form  form to display
   */
  void switchForm(Form form);

  /**
   * Terminates this application.
   */
  void finish();

  /**
   * Retrieves the value of a previously stored preference (even from previous
   * of the same program).
   *
   * @param name  name which was used to store the value under
   * @return  value associated with name
   */
  Variant getPreference(String name);

  /**
   * Stores the given value under given name. The value can be retrieved using
   * the given name any time (even on subsequent runs of the program).
   * 
   * @param name  name to store value under
   * @param value  value to store (must be a primitive value, objects not
   *               allowed)
   */
  void storePreference(String name, Variant value);
}
