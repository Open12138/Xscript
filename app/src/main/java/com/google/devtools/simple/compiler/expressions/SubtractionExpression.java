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
import com.google.devtools.simple.classfiles.Method;

/**
 * This class represents subtraction expression.
 *
 * @author Herbert Czymontek
 */
public final class SubtractionExpression extends ArithmeticExpression {

  /**
   * Creates a subtraction expression.
   * 
   * @param position  source code start position of expression
   * @param leftOperand  operand to subtract from
   * @param rightOperand  operand to subtract
   */
  public SubtractionExpression(long position, Expression leftOperand, Expression rightOperand) {
    super(position, leftOperand, rightOperand);
  }

  @Override
  protected Expression fold(Compiler compiler, FunctionSymbol currentFunction) {
    if (leftOperand instanceof ConstantNumberExpression &&
        rightOperand instanceof ConstantNumberExpression) {
      return new ConstantNumberExpression(getPosition(),
          ((ConstantNumberExpression) leftOperand).value.subtract(
              ((ConstantNumberExpression) rightOperand).value)).resolve(compiler, currentFunction);
    }

    return this;
  }
  
  @Override
  public void generate(Method m) {
    super.generate(m);
    type.generateSubtraction(m);
  }

  @Override
  public String toString() {
    return leftOperand.toString() + " - " + rightOperand.toString();  // COV_NF_LINE
  }
}
