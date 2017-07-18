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

package com.google.devtools.simple.compiler.expressions;

import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.compiler.types.VariantType;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.types.BooleanType;
import com.google.devtools.simple.compiler.types.Type;

/**
 * This class represents a type check operation.
 * 
 * @author Herbert Czymontek
 */
public final class TypeOfExpression extends UnaryExpression {

  // Type to check for
  private Type expectedType;

  /**
   * Creates a new type check expression.
   * 
   * @param position  source code start position of expression
   * @param operand  operand to check type of
   * @param expectedType  expected type of operand
   */
  public TypeOfExpression(long position, Expression operand, Type expectedType) {
    super(position, operand);

    this.expectedType = expectedType;
  }

  @Override
  public Expression resolve(Compiler compiler, FunctionSymbol currentFunction) {
    super.resolve(compiler, currentFunction);
    expectedType.resolve(compiler);
    if ((!expectedType.isArrayType() && !expectedType.isObjectType()) ||
        (!operand.type.isArrayType() && !operand.type.isObjectType())) {
      operand = operand.checkType(compiler, VariantType.variantType);
    }

    type = BooleanType.booleanType;

    return fold(compiler, currentFunction);
  }

  @Override
  public void generate(Method m) {
    operand.generate(m);
    operand.type.generateTypeOf(m, expectedType.internalName());
  }

  @Override
  public String toString() {
    return "TypeOf " + operand.toString() + " Is " + expectedType.toString();  // COV_NF_LINE
  }
}
