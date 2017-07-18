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
 * Superclass for all numerical types.
 * 
 * @author Herbert Czymontek
 */
abstract class NumberType extends Type {
  /**
   * Creates a new numerical type.
   * 
   * @param kind  type kind
   */
  protected NumberType(TypeKind kind) {
    super(kind);
  }

  @Override
  public boolean canConvertTo(Type toType) {
    switch (toType.getKind()) {
      default:
        return false;
  
      case BOOLEAN:
      case BYTE:
      case SHORT:
      case INTEGER:
      case LONG:
      case SINGLE:
      case DOUBLE:
      case STRING:
      case VARIANT:
        return true;
    }
  }

  @Override
  public boolean isScalarType() {
    return true;
  }
}
