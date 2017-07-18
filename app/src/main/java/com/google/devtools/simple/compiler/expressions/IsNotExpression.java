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

/**
 * This class represents a comparison for identity.
 * 
 * <p>The comparison will return {@code true} unless both operands of the
 * comparison are identical. For objects this means that both operands must be
 * the physically different objects (address comparison).
 *
 * @author Herbert Czymontek
 */
public final class IsNotExpression extends ComparisonExpression {

  /**
   * Creates a new identity comparison.
   * 
   * @param position  source code start position of expression
   * @param leftOperand  left operand of comparison
   * @param rightOperand  right operand of comparison
   */
  public IsNotExpression(long position, Expression leftOperand, Expression rightOperand) {
    super(position, leftOperand, rightOperand);
  }

  @Override
  public void generate(Method m) {
    super.generate(m);
    Method.Label trueLabel = Method.newLabel();
    Method.Label contLabel = Method.newLabel();
    leftOperand.type.generateBranchIfCmpNotIdentical(m, trueLabel);
    m.generateInstrLdc(0);
    m.generateInstrGoto(contLabel);
    m.setLabel(trueLabel);
    m.generateInstrLdc(1);
    m.setLabel(contLabel);
  }

  @Override
  public void generateBranchOnFalse(Method m, Method.Label falseLabel) {
    super.generate(m);
    leftOperand.type.generateBranchIfCmpIdentical(m, falseLabel);
  }

  @Override
  public void generateBranchOnTrue(Method m, Method.Label trueLabel) {
    super.generate(m);
    leftOperand.type.generateBranchIfCmpNotIdentical(m, trueLabel);
  }

  @Override
  public String toString() {
    return leftOperand.toString() + " IsNot " + rightOperand.toString();  // COV_NF_LINE
  }
}
