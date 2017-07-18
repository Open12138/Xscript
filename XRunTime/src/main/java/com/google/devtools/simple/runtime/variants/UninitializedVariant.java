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

package com.google.devtools.simple.runtime.variants;

/**
 * Uninitialized variant implementation.
 * 
 * @author Herbert Czymontek
 */
public final class UninitializedVariant extends Variant {

  private static UninitializedVariant UNINITIALIZED_VARIANT = new UninitializedVariant();

  /**
   * Factory method for creating object variants.
   * 
   * @return  new uninitialized variant
   */
  public static final UninitializedVariant getUninitializedVariant() {
    return UNINITIALIZED_VARIANT;
  }

  /*
   * Creates a new uninitialized variant.
   */
  private UninitializedVariant() {
    super(VARIANT_UNINITIALIZED);
  }
}
