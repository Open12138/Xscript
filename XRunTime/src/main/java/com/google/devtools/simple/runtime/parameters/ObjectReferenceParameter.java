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

/**
 * Implementation of support class for Simple Object, String, Variant and array
 * reference parameters.
 *
 * @param <T>  type of object reference
 *
 * @author Herbert Czymontek
 */
public class ObjectReferenceParameter<T> extends ReferenceParameter {

  // Value of referenced variable
  private T value;

  /**
   * Creates a new object or array reference parameter.
   * 
   * @param value  initial value of reference
   */
  public ObjectReferenceParameter(T value) {
    set(value);
  }

  /**
   * Returns the current value of the reference.
   * 
   * @return  current value of reference parameter
   */
  public T get() {
    return value;
  }

  /**
   * Changes the value of the reference.
   * 
   * @param value  new value of reference parameter
   */
  public void set(T value) {
    this.value = value;
  }
}
