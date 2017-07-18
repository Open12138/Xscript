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

import com.google.devtools.simple.compiler.expressions.Expression;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.compiler.Compiler;

/**
 * Superclass for all iterative loop statements (like 'For'-statements).
 * 
 * @author Herbert Czymontek
 */
public abstract class IterativeLoopStatement extends LoopStatement {
  // Variable over which to iterate
  protected Expression loopVarExpr;

  // Initialization expression for loop variable
  protected Expression initExpr;

  /**
   * Creates a new iterative loop statement.
   *
   * @param position  source code start position of statement
   * @param loopVarExpr  variable over which to iterate
   * @param initExpr  initialization expression for loop variable
   * @param loopStatements  statements in loop body
   */
  public IterativeLoopStatement(long position, Expression loopVarExpr, Expression initExpr,
      StatementBlock loopStatements) {
    super(position, loopStatements);
    this.loopVarExpr = loopVarExpr;
    this.initExpr = initExpr;
  }

  @Override
  public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
    super.resolve(compiler, currentFunction);

    loopVarExpr = loopVarExpr.resolve(compiler, currentFunction);
    initExpr = initExpr.resolve(compiler, currentFunction);
  }
}
