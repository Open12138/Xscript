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
import com.google.devtools.simple.compiler.types.StringType;
import com.google.devtools.simple.classfiles.Method;

/**
 * This class represents the concatenation expression of two string values.
 * 
 * @author Herbert Czymontek
 */
public final class ConcatenationExpression extends BinaryExpression {

  /**
   * Creates a new concatenation expression.
   *
   * @param position  source code start position of expression
   * @param leftOperand  left operand to concatenate
   * @param rightOperand  right operand to concatenate
   */
  public ConcatenationExpression(long position, Expression leftOperand, Expression rightOperand) {
    super(position, leftOperand, rightOperand);
  }

  @Override
  protected Expression fold(Compiler compiler, FunctionSymbol currentFunction) {
    if (leftOperand instanceof ConstantStringExpression &&
        rightOperand instanceof ConstantStringExpression) {
      return new ConstantStringExpression(getPosition(),
          ((ConstantStringExpression)leftOperand).value +
          ((ConstantStringExpression)rightOperand).value).resolve(compiler, currentFunction);
    }

    return this;
  }

  @Override
  public Expression resolve(Compiler compiler, FunctionSymbol currentFunction) {
    super.resolve(compiler, currentFunction);

    type = StringType.stringType;
    rightOperand = rightOperand.checkType(compiler, type);
    leftOperand = leftOperand.checkType(compiler, type);

    return fold(compiler, currentFunction);
  }

  @Override
  public void generate(Method m) {
    // Normally I would define a concat code generation operation for the string type. But
    // concatenating strings is an expensive operation. Optimization requires additional
    // information which happens to be available here!
    // TODO: generate better code
    m.generateInstrNew("java/lang/StringBuilder");
    m.generateInstrDup();
    m.generateInstrInvokespecial("java/lang/StringBuilder", "<init>", "()V");
    leftOperand.generate(m);
    m.generateInstrInvokevirtual("java/lang/StringBuilder", "append",
        "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
    rightOperand.generate(m);
    m.generateInstrInvokevirtual("java/lang/StringBuilder", "append",
        "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
    m.generateInstrInvokevirtual("java/lang/StringBuilder", "toString",
        "()Ljava/lang/String;");
  }

  @Override
  public String toString() {
    return leftOperand.toString() + " & " + rightOperand.toString();  // COV_NF_LINE
  }
}
