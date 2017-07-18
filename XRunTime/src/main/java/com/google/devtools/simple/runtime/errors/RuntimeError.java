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

package com.google.devtools.simple.runtime.errors;

import com.google.devtools.simple.runtime.annotations.SimpleObject;

/**
 * Superclass of all Simple runtime errors.
 * 
 * @author Herbert Czymontek
 */
@SuppressWarnings("serial")
@SimpleObject
public abstract class RuntimeError extends RuntimeException {

  /**
   * Creates a runtime error.
   */
  protected RuntimeError() {
  }
  
  /**
   * Creates a runtime error with a more detailed error message.
   * 
   * @param message  detailed error message
   */
  protected RuntimeError(String message) {
    super(message);
  }

  /**
   * Converts a Java {@link Throwable} into a Simple runtime error.
   * 
   * @param throwable  Java throwable to be converted (may be a Simple runtime
   *                   error already)
   * @return  Simple runtime error
   */
  public static RuntimeError convertToRuntimeError(Throwable throwable) {
    if (throwable instanceof RuntimeError) {
      return (RuntimeError) throwable;
    }

    // Conversions of Java exceptions
    if (throwable instanceof ArrayIndexOutOfBoundsException) {
      return new IndexOutOfBoundsError();
    }
    if (throwable instanceof IllegalArgumentException) {
      return new IllegalArgumentError();
    }
    if (throwable instanceof NullPointerException) {
      return new UninitializedInstanceError();
    }

    throw new UnsupportedOperationException(throwable);
  }
}
