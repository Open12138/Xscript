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
 * This is the superclass for expressions with two operands.
 * 
 * @author Herbert Czymontek
 */
public abstract class BinaryExpression extends Expression {
  // First or left operand
  protected Expression leftOperand;

  // Second or right operand
  protected Expression rightOperand;

  /**
   * Creates an expression with two operands.
   * 
   * @param position  source code start position of expression
   * @param leftOperand  first or left operand
   * @param rightOperand  second or right operand
   */
  public BinaryExpression(long position, Expression leftOperand, Expression rightOperand) {
    super(position);

    this.leftOperand = leftOperand;
    this.rightOperand = rightOperand;
  }

  @Override
  public Expression resolve(Compiler compiler, FunctionSymbol currentFunction) {
    leftOperand = leftOperand.resolve(compiler, currentFunction);
    rightOperand = rightOperand.resolve(compiler, currentFunction);

    return this;
  }

  @Override
  public void generate(Method m) {
    leftOperand.generate(m);
    rightOperand.generate(m);
  }
}
