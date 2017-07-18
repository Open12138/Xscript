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
import com.google.devtools.simple.runtime.variants.Variant;

import java.util.Locale;

/**
 * Implementation of various conversion related runtime functions.
 * 
 * @author Herbert Czymontek
 */
@SimpleObject
public final class Conversions {

  private Conversions() {
  }

  /**
   * Returns the unicode value of the first character of the given string.
   *
   * @param str  string to convert first character of
   * @return  unicode value of first character of {@code str}
   */
  @SimpleFunction
  public static int Asc(String str) {
    try {
      return str.charAt(0);
    } catch (IndexOutOfBoundsException e) {
      // Will be converted to a Simple runtime error at runtime
      throw new IllegalArgumentException("String length for Asc() must be 1 or greater");
    }
  }

  /**
   * Returns a string for the given unicode value.
   * 
   * @param value  unicode value to convert into a string
   * @return  string consisting of given unicode value
   */
  @SimpleFunction
  public static String Chr(int value) {
    return Character.toString((char)value);
  }

  /**
   * Returns a string containing the hexadecimal value for the given value.
   * If the given value is not w whole number then its integer part will be
   * used.
   * 
   * @param v  value
   * @return  string with hexadecimal value of {@code v}
   */
  @SimpleFunction
  public static String Hex(Variant v) {
    return Long.toHexString(v.getLong()).toUpperCase(Locale.ENGLISH);
  }
}
