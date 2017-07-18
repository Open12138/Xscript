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
import com.google.devtools.simple.compiler.expressions.synthetic.ConversionExpression;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.compiler.types.Type;
import com.google.devtools.simple.compiler.types.VariantType;

/**
 * Superclass for all expressions.
 *
 * @author Herbert Czymontek
 */
public abstract class Expression {

  // Source code start position of expression
  private final long position; 

  // Type of expression (can be null for typeless expressions)
  protected Type type;

  /**
   * Creates a new expression.
   * 
   * @param position  source code start position of expression
   */
  public Expression(long position) {
    this.position = position;
  }

  /**
   * Returns the source code position of the expression.
   * 
   * @return  source code start position of expression
   */
  public long getPosition() {
    return position;
  }

  /**
   * Returns the type of the expression (can be {@code null} for typeless
   * expressions).
   * 
   * @return  type of expression
   */
  public Type getType() {
    return type.getType();
  }
  
  /**
   * Resolves any identifiers or types involved in the expression. Applies any
   * necessary type conversions. Also initiates expression folding. After the
   * resolution the expression is ready to have code generated for it.
   * 
   * @param compiler  current compiler instance
   * @param currentFunction  current function being resolved
   *                         (can be {code null})
   * @return  resolved expression (this can be a different expression from
   *          the original expression, e.g. after expression folding or type
   *          conversion)
   */
  public abstract Expression resolve(Compiler compiler, FunctionSymbol currentFunction);

  /**
   * Simplifies the expression by trying to apply constant folding.
   * 
   * @param compiler  current compiler instance
   * @param currentFunction  current function being resolved
   *                         (can be {code null})
   * @return  simplified expression (this can be a different expression from
   *          the original expression)
   */
  protected Expression fold(Compiler compiler, FunctionSymbol currentFunction) {
    return this;
  }

  /**
   * Checks and converts the type of an expression to the specified type (if
   * necessary).
   * 
   * @param compiler  current compiler instance
   * @param expectedType  expected type
   * @return  expression with the expected type (this can be a different
   *          expression from the original expression)
   */
  public final Expression checkType(Compiler compiler, Type expectedType) {
    // If the type already matches the expected type, we are done
    if (getType().equals(expectedType)) {
      return this;
    }

    // Otherwise try to convert to the expected type
    return ConversionExpression.convert(compiler, this, expectedType);
  }

  /**
   * Returns true if the expression is an lvalue.
   * 
   * @return  {@code true} for lvalues, {@code false} otherwise
   */
  public boolean isAssignable() {
    return false;
  }

  /**
   * Checks whether the expression is an assignment expression.
   * 
   * @return  {@code true} if the expression is an assignment expression
   */
  public boolean isAssignmentExpression() {
    return false;
  }

  /**
   * Checks whether the expression is a call expression.
   * 
   * @return  {@code true} if the expression is a call expression
   */
  public boolean isCallExpression() {
    return false;
  }

  /**
   * Generates code for the expression.
   * 
   * @param m  method to generate the code for
   */
  public abstract void generate(Method m);

  /**
   * Generates code to prepare for a write operation.
   * 
   * @param m  method to generate the code for
   */
  public void generatePrepareWrite(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates code for a write operation.
   * 
   * @param m  method to generate the code for
   */
  public void generateWrite(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates code to prepare for a method invocation.
   * 
   * @param m  method to generate the code for
   */
  public void generatePrepareInvoke(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates code to perform a method invocation.
   * 
   * @param m  method to generate the code for
   */
  public void generateInvoke(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  /**
   * Generates code to branch to a label if the expression evaluates to
   * {@code false}.
   *
   * @param m  method to generate the code for
   * @param falseLabel  label to branch to
   */
  public void generateBranchOnFalse(Method m, Method.Label falseLabel) {
    generate(m);
    m.generateInstrIfeq(falseLabel);
  }

  /**
   * Generates code to branch to a label if the expression evaluates to
   * {@code true}.
   * 
   * @param m  method to generate the code for
   * @param trueLabel  label to branch to
   */
  public void generateBranchOnTrue(Method m, Method.Label trueLabel) {
    generate(m);
    m.generateInstrIfne(trueLabel);
  }

  /**
   * Reports a 'scalar type needed' error.
   * 
   * @param compiler  current compiler instance
   * @param operand  operand to report error for
   * @return  same as operand
   */
  protected Expression reportScalarTypeNeededError(Compiler compiler, Expression operand) {
    compiler.error(operand.getPosition(), Error.errScalarTypeNeededForOperand);
    type = VariantType.variantType;
    return this;
  }

  /**
   * Reports a 'scalar integer type needed' error.
   * 
   * @param compiler  current compiler instance
   * @param operand  operand to report error for
   * @return  same as operand
   */
  protected Expression reportScalarIntegerTypeNeededError(Compiler compiler, Expression operand) {
    compiler.error(operand.getPosition(), Error.errScalarIntegerTypeNeededForOperand);
    type = VariantType.variantType;
    return this;
  }
}
