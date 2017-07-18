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

import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;

/**
 * This class represents an identity operation (via a plus operator).
 *
 * @author Herbert Czymontek
 */
public final class IdentityExpression extends UnaryExpression {

  /**
   * Creates a new identity expression.
   * 
   * @param position  source code start position of expression
   * @param operand  operand prefixed with a plus operator
   */
  public IdentityExpression(long position, Expression operand) {
    super(position, operand);
  }

  @Override
  public Expression resolve(Compiler compiler, FunctionSymbol currentFunction) {
    super.resolve(compiler, currentFunction);

    type = operand.type;
    if (!type.isScalarType()) {
      return reportScalarTypeNeededError(compiler, operand);
    }

    return this;
  }

  @Override
  public String toString() {
    return '+' + operand.toString();  // COV_NF_LINE
  }
}
