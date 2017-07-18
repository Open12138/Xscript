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
import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.Error;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.compiler.symbols.ObjectSymbol;

/**
 * This class represents an assignment expression.
 * 
 * @author Herbert Czymontek
 */
public final class AssignmentExpression extends BinaryExpression {

  /**
   * Creates a new assignment expression.
   * 
   * @param position  source code start position of expression
   * @param leftOperand  left operand (must be an lvalue)
   * @param rightOperand  right operand
   */
  public AssignmentExpression(long position, Expression leftOperand, Expression rightOperand) {
    super(position, leftOperand, rightOperand);
  }

  @Override
  public Expression resolve(Compiler compiler, FunctionSymbol currentFunction) {
    super.resolve(compiler, currentFunction);
    
    if (!leftOperand.isAssignable()) {
      compiler.error(leftOperand.getPosition(), Error.errOperandNotAssignable);
    }

    // Need to make sure that it is a value and not just a type name
    if (rightOperand instanceof IdentifierExpression &&
        ((IdentifierExpression) rightOperand).resolvedIdentifier instanceof ObjectSymbol) {
      compiler.error(leftOperand.getPosition(), Error.errValueExpected);
    }

    type = leftOperand.type;
    rightOperand = rightOperand.checkType(compiler, type);

    return fold(compiler, currentFunction);
  }

  @Override
  public boolean isAssignmentExpression() {
    return true;
  }
  
  @Override
  public void generate(Method m) {
    leftOperand.generatePrepareWrite(m);
    rightOperand.generate(m);
    if (type.isObjectType() || type.isArrayType()) {
      m.generateInstrCheckcast(type.internalName());
    }
    leftOperand.generateWrite(m);
  }

  @Override
  public String toString() {
    return leftOperand.toString() + " = " + rightOperand.toString();  // COV_NF_LINE
  }
}
