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

package com.google.devtools.simple.compiler.types;

/**
 * Superclass for all integer types.
 * 
 * @author Herbert Czymontek
 */
public abstract class IntegerNumberType extends NumberType {

  /**
   * Creates a new integer type.
   * 
   * @param kind  type kind
   */
  protected IntegerNumberType(TypeKind kind) {
    super(kind);
  }

  /**
   * Returns the number of bits used by values of the type.
   * 
   * @return  number of bits used by values of type
   */
  public abstract int bitsize();

  @Override
  public boolean isScalarIntegerType() {
    return true;
  }
}
