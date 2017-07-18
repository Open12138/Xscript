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
import com.google.devtools.simple.util.Preconditions;

/**
 * This class represents a not expression.
 *
 * @author Herbert Czymontek
 */
public final class NotExpression extends UnaryExpression {

  /**
   * Creates a not expression.
   * 
   * @param position  source code start position of expression
   * @param operand  operand to apply not operation to
   */
  public NotExpression(long position, Expression operand) {
    super(position, operand);
  }

  @Override
  public Expression resolve(Compiler compiler, FunctionSymbol currentFunction) {
    super.resolve(compiler, currentFunction);

    type = operand.type;
    if (!type.isScalarIntegerType()) {
      return reportScalarIntegerTypeNeededError(compiler, operand);
    }

    return fold(compiler, currentFunction);
  }

  @Override
  public void generate(Method m) {
    operand.generate(m);
    type.generateBitNot(m);
  }

  @Override
  public void generateBranchOnFalse(Method m, Method.Label falseLabel) {
    Preconditions.checkState(type.isBooleanType());

    operand.generate(m);
    m.generateInstrIfne(falseLabel);
  }

  @Override
  public void generateBranchOnTrue(Method m, Method.Label trueLabel) {
    Preconditions.checkState(type.isBooleanType());

    operand.generate(m);
    m.generateInstrIfeq(trueLabel);
  }

  @Override
  public String toString() {
    return "Not " + operand.toString();  // COV_NF_LINE
  }
}

