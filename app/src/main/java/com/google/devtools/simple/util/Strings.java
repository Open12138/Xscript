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

package com.google.devtools.simple.util;

import java.util.Arrays;

/**
 * Helper class for String related operations.
 *
 * @author Herbert Czymontek
 */
public final class Strings {
  private Strings() {
  }

  /**
   * Returns a string consisting of the joined string array elements,
   * separated by the delimiter.
   * 
   * @param delimiter  separates individual strings in the joined string
   * @param objects  strings to join (will invoke toString() on the objects)
   * @return  string resulting from joining the individual strings
   */
  public static String join(String delimiter, final Object[] objects) {
    return join(delimiter, Arrays.asList(objects));
  }

  /**
   * Returns a string consisting of the joined strings, separated by the
   * delimiter.
   * 
   * @param delimiter  separates array elements in created string
   * @param objects  strings to join (will invoke toString() on the objects)
   * @return  string created of joined string array elements
   */
  public static String join(String delimiter, Iterable<?> objects) {
    Preconditions.checkNotNull(delimiter);
    Preconditions.checkNotNull(objects);

    StringBuilder sb = new StringBuilder();
    String separator = "";
    for (Object object : objects) {
      sb.append(separator);
      sb.append(object.toString());
      separator = delimiter;
    }
    return sb.toString();
  }
}
