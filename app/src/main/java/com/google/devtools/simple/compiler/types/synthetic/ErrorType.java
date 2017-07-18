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

package com.google.devtools.simple.compiler.types.synthetic;

import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.types.Type;

/**
 * Type for error situations.
 *
 * @author Herbert Czymontek
 */
public final class ErrorType extends Type {

  /**
   * Singleton instance of a type for error situations.
   */
  public static final ErrorType errorType = new ErrorType();

  private ErrorType() {
    super(TypeKind.ERROR);
  }

  @Override
  public boolean equals(Object obj) {
    return true;
  }

  @Override
  public int hashCode() {
    return 1;
  }

  @Override
  public boolean isArrayType() {
    return true;
  }

  @Override
  public boolean isFunctionType() {
    return true;
  }

  @Override
  public boolean isObjectType() {
    return true;
  }

  @Override
  public boolean isBooleanType() {
    return true;
  }

  @Override
  public boolean isScalarIntegerType() {
    return true;
  }

  @Override
  public boolean isLongType() {
    return true;
  }

  @Override
  public boolean isScalarType() {
    return true;
  }

  @Override
  public boolean isVariantType() {
    return true;
  }

  @Override
  public boolean isErrorType() {
    return true;
  }

  @Override
  public boolean canConvertTo(Type toType) {
    return true;
  }

  @Override
  public String internalName() {
    Compiler.internalError();
    return "<error>";
  }

  @Override
  public String referenceInternalName() {
    Compiler.internalError();
    return "<error>";
  }

  @Override
  public String referenceValueSignature() {
    Compiler.internalError();
    return "<error>";
  }
}
