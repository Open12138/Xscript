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
import com.google.devtools.simple.compiler.types.DoubleType;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.types.Type;

import java.math.MathContext;

/**
 * This class represents an division expression.
 * 
 * <p>The result of the devision is a floating point number regardless of the
 * type of the operands.
 *
 * @author Herbert Czymontek
 */
public final class DivisionExpression extends ArithmeticExpression {

  /**
   * Creates a new division expression.
   *
   * @param position  source code start position of expression
   * @param leftOperand  operand to divide
   * @param rightOperand  operand to divide by
   */
  public DivisionExpression(long position, Expression leftOperand, Expression rightOperand) {
    super(position, leftOperand, rightOperand);
  }

  @Override
  protected Expression fold(Compiler compiler, FunctionSymbol currentFunction) {
    if (leftOperand instanceof ConstantNumberExpression &&
        rightOperand instanceof ConstantNumberExpression) {
      return new ConstantNumberExpression(getPosition(),
          ((ConstantNumberExpression) leftOperand).value.divide(
              ((ConstantNumberExpression) rightOperand).value, MathContext.DECIMAL128)).
              resolve(compiler, currentFunction);
    }

    return this;
  }

  @Override
  public Expression resolve(Compiler compiler, FunctionSymbol currentFunction) {
    leftOperand.resolve(compiler, currentFunction);
    Type leftType = leftOperand.type;
    if (!leftType.isScalarType()) {
      return reportScalarTypeNeededError(compiler, leftOperand);
    }

    rightOperand.resolve(compiler, currentFunction);
    Type rightType = rightOperand.type;
    if (!rightType.isScalarType()) {
      return reportScalarTypeNeededError(compiler, rightOperand);
    }

    type = DoubleType.doubleType;
    leftOperand = leftOperand.checkType(compiler, type);
    rightOperand = rightOperand.checkType(compiler, type);
    
    return fold(compiler, currentFunction);
  }

  @Override
  public void generate(Method m) {
    super.generate(m);
    type.generateDivision(m);
  }

  @Override
  public String toString() {
    return leftOperand.toString() + " / " + rightOperand.toString();  // COV_NF_LINE
  }
}
