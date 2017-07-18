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

import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.util.Preconditions;

/**
 * This class represents either a logical or a bitwise XOR expression,
 * depending on the type of the operands.
 *
 * @author Herbert Czymontek
 */
public final class XorExpression extends LogicalOrBitOpExpression {

  /**
   * Creates a new XOR expression.
   * 
   * @param position  source code start position of expression
   * @param leftOperand  left operand
   * @param rightOperand  right operand
   */
  public XorExpression(long position, Expression leftOperand, Expression rightOperand) {
    super(position, leftOperand, rightOperand);
  }

  @Override
  public void generate(Method m) {
    super.generate(m);
    type.generateBitXor(m);
  }

  @Override
  public void generateBranchOnFalse(Method m, Method.Label falseLabel) {
    Preconditions.checkArgument(type.isBooleanType());

    generate(m);
    m.generateInstrIfeq(falseLabel);
  }

  @Override
  public void generateBranchOnTrue(Method m, Method.Label trueLabel) {
    Preconditions.checkArgument(type.isBooleanType());

    generate(m);
    m.generateInstrIfne(trueLabel);
  }

  @Override
  public String toString() {
    return leftOperand.toString() + " Xor " + rightOperand.toString();  // COV_NF_LINE
  }
}
