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

package com.google.devtools.simple.runtime.components;

import com.google.devtools.simple.runtime.annotations.SimpleObject;
import com.google.devtools.simple.runtime.annotations.SimpleProperty;

/**
 * A layout component allowing subcomponents to be placed in tabular form.
 *
 * @author Herbert Czymontek
 */
@SimpleObject
public interface TableLayout extends Layout {

  /**
   * Columns property setter method.
   *
   * @param cols  number of columns in this layout
   */
  @SimpleProperty
  void Columns(int cols);

  /**
   * Rows property setter method.
   *
   * @param rows  number of rows in this layout
   */
  @SimpleProperty
  void Rows(int rows);
}
