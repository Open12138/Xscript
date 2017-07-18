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

package com.google.devtools.simple.runtime.parameters;

import java.util.Calendar;

/**
 * Implementation of support class for Simple Date reference parameters.
 * 
 * @author Herbert Czymontek
 */
public final class DateReferenceParameter extends ReferenceParameter {

  // Value of referenced variable
  private Calendar value;

  /**
   * Creates a new Date reference parameter.
   * 
   * @param value  initial value of reference
   */
  public DateReferenceParameter(Calendar value) {
    set(value);
  }

  /**
   * Returns the current value of the reference.
   * 
   * @return  current value of reference parameter
   */
  public Calendar get() {
    return value;
  }

  /**
   * Changes the value of the reference.
   * 
   * @param value  new value of reference parameter
   */
  public void set(Calendar value) {
    this.value = value;
  }
}
