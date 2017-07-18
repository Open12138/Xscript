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
import com.google.devtools.simple.compiler.expressions.Expression;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.compiler.types.BooleanType;

/**
 * Superclass for all loop statements running while or until a condition is
 * met (unlike iterative loops).
 * 
 * @author Herbert Czymontek
 */
public abstract class ConditionalLoopStatement extends LoopStatement {
  // Loop condition
  protected Expression condition;

  /**
   * Creates a new conditional loop statement.
   * 
   * @param position  source code start position of statement
   * @param condition  loop condition
   * @param loopStatements  statements in loop body
   */
  public ConditionalLoopStatement(long position, Expression condition,
      StatementBlock loopStatements) {
    super(position, loopStatements);
    this.condition = condition;
  }

  @Override
  public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
    super.resolve(compiler, currentFunction);
    condition = condition.resolve(compiler, currentFunction).checkType(compiler, 
        BooleanType.booleanType);
  }
}
