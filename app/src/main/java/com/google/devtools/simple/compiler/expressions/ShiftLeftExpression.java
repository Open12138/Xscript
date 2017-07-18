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

import java.math.BigDecimal;

/**
 * This class represents shift left expression.
 *
 * @author Herbert Czymontek
 */
public final class ShiftLeftExpression extends ShiftExpression {

  /**
   * Creates a new left shift expression.
   * 
   * @param position  source code start position of expression
   * @param leftOperand  operand to be shifted
   * @param rightOperand  operand to shift by
   */
  public ShiftLeftExpression(long position, Expression leftOperand, Expression rightOperand) {
    super(position, leftOperand, rightOperand);
  }

  @Override
  protected Expression fold(Compiler compiler, FunctionSymbol currentFunction) {
    if (leftOperand instanceof ConstantNumberExpression &&
        rightOperand instanceof ConstantNumberExpression) {
      return new ConstantNumberExpression(getPosition(), new BigDecimal(
          ((ConstantNumberExpression) leftOperand).value.toBigInteger().shiftLeft(
              ((ConstantNumberExpression) rightOperand).value.toBigInteger().intValue()))).
              resolve(compiler, currentFunction);
    }

    return this;
  }

  @Override
  public void generate(Method m) {
    super.generate(m);
    type.generateShiftLeft(m);
  }

  @Override
  public String toString() {
    return leftOperand.toString() + " << " + rightOperand.toString();  // COV_NF_LINE
  }
}
