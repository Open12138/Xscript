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

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of various array related runtime functions.
 * 
 * @author Herbert Czymontek
 */
@SimpleObject
public final class Arrays {

  private Arrays() {  // COV_NF_LINE
  }                   // COV_NF_LINE

  /**
   * Filters the contents of an array.
   * 
   * @param array  array to search in
   * @param str  substring to search for in the array
   * @param include  if {@code true} then include matching strings in the
   *                 result, otherwise exclude them
   * @return  array containing (non-)matching array entries
   */
  @SimpleFunction
  public static String[] Filter(String[] array, String str, boolean include) {
    if (str == null) {
      throw new NullPointerException();
    }

    Set<String> result = new HashSet<String>();
    for (String a : array) {
      if (a.contains(str) == include) {
        result.add(a);
      }
    }
    return result.toArray(new String[result.size()]);
  }

  /**
   * Appends array elements to a become a single string.
   * 
   * @param array  array containing strings to be appended
   * @param separator  string append between array elements
   * @return  string containing appended array elements
   */
  @SimpleFunction
  public static String Join(String[] array, String separator) {
    if (separator == null) {
      throw new NullPointerException();
    }

    StringBuilder sb = new StringBuilder();
    String sep = "";
    for (String a : array) {
     sb.append(sep).append(a);
     sep = separator;
    }
    return sb.toString();
  }

  /**
   * Splits up the given string where a separator is being found.
   * 
   * @param str  string to be split up
   * @param separator  separator to look for
   * @param count  number of times 
   * @return  array containing split string
   */
  @SimpleFunction
  public static String[] Split(String str, String separator, int count) {
    if (separator == null) {
      throw new NullPointerException();
    }

    if (count <= 0) {
      throw new IllegalArgumentException("Count for Split() out of range. Should greater than 0.");
    }

    return str.split("\\Q" + separator + "\\E", count);
  }

  /**
   * Return the size of an array dimension.
   * 
   * @param array  array whose size is requested
   * @param dim  dimension (1 for the first dimension, and so on)
   * @return  size of the array dimension
   */
  @SimpleFunction
  public static int UBound(Variant array, int dim) {
    if (dim <= 0) {
      throw new IllegalArgumentException("Dimension for UBound() out of range. " +
          "Should be greater than 0.");
    }

    Object arr = array.getArray();
    while (--dim > 0) {
      arr = Array.get(arr, 0);
    }

    return Array.getLength(arr);
  }
}
