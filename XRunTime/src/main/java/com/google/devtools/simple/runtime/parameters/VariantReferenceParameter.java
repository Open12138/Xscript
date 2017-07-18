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

import com.google.devtools.simple.runtime.variants.Variant;

/**
 * Implementation of support class for Simple Variant reference parameters.
 * 
 * @author Herbert Czymontek
 */
public final class VariantReferenceParameter extends ReferenceParameter {

  // Value of referenced variable
  private Variant value;

  /**
   * Creates a new Variant reference parameter.
   * 
   * @param value  initial value of reference
   */
  public VariantReferenceParameter(Variant value) {
    set(value);
  }

  /**
   * Returns the current value of the reference.
   * 
   * @return  current value of reference parameter
   */
  public Variant get() {
    return value;
  }

  /**
   * Changes the value of the reference.
   * 
   * @param value  new value of reference parameter
   */
  public void set(Variant value) {
    this.value = value;
  }
}
