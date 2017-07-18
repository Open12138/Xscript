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

package com.google.devtools.simple.compiler.statements;

import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.Error;
import com.google.devtools.simple.compiler.expressions.Expression;
import com.google.devtools.simple.compiler.scanner.TokenKind;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.compiler.types.Type;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.symbols.LocalVariableSymbol;

/**
 * This class implements the For-Next-Statement that iteratively loops over an
 * index variable.
 * 
 * @author Herbert Czymontek
 */
public final class ForNextStatement extends IterativeLoopStatement {
  // End value for loop variable
  private Expression endExpr;

  // Increment value for loop variable
  private Expression stepExpr;

  // Temporary variables to hold results of evaluated end and step expressions
  private LocalVariableSymbol endExprTemp;
  private LocalVariableSymbol stepExprTemp;

  /**
   * Creates a new For-Next-statement.
   *
   * @param position  source code start position of statement
   * @param loopVarExpr  variable over which to iterate
   * @param initExpr  initialization expression for loop variable
   * @param endExpr  end value of loop variable
   * @param stepExpr  increment value for loop variable
   * @param loopStatements  statements in loop body
   */
  public ForNextStatement(long position, Expression loopVarExpr, Expression initExpr,
      Expression endExpr, Expression stepExpr, StatementBlock loopStatements) {
    super(position, loopVarExpr, initExpr, loopStatements);

    this.endExpr = endExpr;
    this.stepExpr = stepExpr;
  }

  @Override
  public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
    super.resolve(compiler, currentFunction);

    Type type = loopVarExpr.getType();
    if (!type.isScalarType()) {
      compiler.error(loopVarExpr.getPosition(), Error.errScalarTypeNeededInForNext);
    }

    initExpr = initExpr.checkType(compiler, type);

    endExpr = endExpr.resolve(compiler, currentFunction).checkType(compiler, type);
    stepExpr = stepExpr.resolve(compiler, currentFunction).checkType(compiler, type);

    endExprTemp = currentFunction.addTempVariable(type);
    stepExprTemp = currentFunction.addTempVariable(type);
  }

  @Override
  protected TokenKind getLoopStartToken() {
    return TokenKind.TOK_FOR;
  }

  @Override
  public void generate(Method m) {
    Type type = loopVarExpr.getType();

    // Generate initialization of loop variable
    generateLineNumberInformation(m);
    loopVarExpr.generatePrepareWrite(m);
    initExpr.generate(m);
    loopVarExpr.generateWrite(m);

    // Evaluate end and step expression just once and store the results in a temporary variable.
    // TODO: generate better code
    endExpr.generate(m);
    type.generateStoreLocal(m, endExprTemp);
    stepExpr.generate(m);
    type.generateStoreLocal(m, stepExprTemp);

    // Generate the loop body.
    Method.Label testLabel = Method.newLabel();
    m.generateInstrGoto(testLabel);

    Method.Label loopLabel = Method.newLabel();
    m.setLabel(loopLabel);
    loopStatements.generate(m);

    // Generate step
    generateLineNumberInformation(m);
    loopVarExpr.generatePrepareWrite(m);
    loopVarExpr.generate(m);
    type.generateLoadLocal(m, stepExprTemp);
    type.generateAddition(m);
    loopVarExpr.generateWrite(m);

    // Generate test
    m.setLabel(testLabel);
    loopVarExpr.generate(m);
    type.generateLoadLocal(m, endExprTemp);

    // TODO: generate better code - optimize this for constant step values
    type.generateDefaultInitializationValue(m);
    type.generateLoadLocal(m, stepExprTemp);
    Method.Label negativeStepLabel = Method.newLabel();
    type.generateBranchIfCmpGreaterOrEqual(m, negativeStepLabel);

    type.generateBranchIfCmpLessOrEqual(m, loopLabel);

    m.generateInstrGoto(exitLabel);

    m.setLabel(negativeStepLabel);
    type.generateBranchIfCmpGreaterOrEqual(m, loopLabel);

    m.setLabel(exitLabel);
  }

  @Override
  public String toString() {
    return "For " + loopVarExpr.toString() + " = " + initExpr.toString() +  // COV_NF_LINE
        " To " + endExpr.toString() + " Step " + stepExpr.toString();
  }
}
