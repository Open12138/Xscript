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

package com.google.devtools.simple.runtime.helpers;

import com.google.devtools.simple.runtime.Arrays;
import com.google.devtools.simple.runtime.collections.Collection;
import com.google.devtools.simple.runtime.variants.ArrayVariant;
import com.google.devtools.simple.runtime.variants.IntegerVariant;
import com.google.devtools.simple.runtime.variants.ObjectVariant;
import com.google.devtools.simple.runtime.variants.Variant;

/**
 * Helper methods for statements that are too complicated to inline.
 * 
 * @author Herbert Czymontek
 */
public final class StmtHelpers {

  private StmtHelpers() {  // COV_NF_LINE
  }                        // COV_NF_LINE

  /**
   * Returns the element count of the collection or array from the given
   * variant.
   *
   * @param v  variant containing a collection or array
   * @return  element count
   */
  public static int forEachCount(Variant v) {
    if (v instanceof ArrayVariant) {
      return Arrays.UBound(v, 1);
    } else {
      // Might cause a class cast exception which will be converted to a runtime error by Simple
      // exception handlers
      return ((Collection) ((ObjectVariant) v).getObject()).Count();
    }
  }

  /**
   * Returns the element with the given index from the the collection or array
   * from the given variant.
   *
   * @param v  variant containing a collection or array
   * @param index  element index
   * @return  element
   */
  public static Variant forEachItem(Variant v, int index) {
    if (v instanceof ArrayVariant) {
      return ((ArrayVariant) v).array(new Variant[] { IntegerVariant.getIntegerVariant(index) });
    } else {
      // Will not cause any exception because forEachCount() would have already thrown it
      return ((Collection) ((ObjectVariant) v).getObject()).Item(index);
    }
  }
}
