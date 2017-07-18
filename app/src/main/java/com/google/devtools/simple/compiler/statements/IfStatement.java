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
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.types.BooleanType;

/**
 * This class implements any flavor of If-statements.
 * 
 * @author Herbert Czymontek
 */
public final class IfStatement extends Statement {
  // Condition
  private Expression condition;

  // Statements in then-part
  private StatementBlock thenStatements;

  // Statements in else-part (can be null)
  private StatementBlock elseStatements;

  /**
   * Creates a new If-statement.
   *
   * @param position  source code start position of statement
   * @param condition  condition
   * @param thenStatements  statements in then-part
   * @param elseStatements  statements in else-part (can be null)
   */
  public IfStatement(long position, Expression condition, StatementBlock thenStatements,
      StatementBlock elseStatements) {
    super(position);
    this.condition = condition;
    this.thenStatements = thenStatements;
    this.elseStatements = elseStatements;
  }

  @Override
  public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
    condition = condition.resolve(compiler, currentFunction).checkType(compiler,
        BooleanType.booleanType);
    thenStatements.resolve(compiler, currentFunction, this);
    if (elseStatements != null) {
      elseStatements.resolve(compiler, currentFunction, this);
    }
  }

  @Override
  public void generate(Method m) {
    generateLineNumberInformation(m);
    Method.Label endLabel = Method.newLabel();
    if (elseStatements == null) {
      condition.generateBranchOnFalse(m, endLabel);
      thenStatements.generate(m);
      m.setLabel(endLabel);
    } else {
      Method.Label elseLabel = Method.newLabel();
      condition.generateBranchOnFalse(m, elseLabel);
      thenStatements.generate(m);
      m.generateInstrGoto(endLabel);
      m.setLabel(elseLabel);
      elseStatements.generate(m);
      m.setLabel(endLabel);
    }
  }

  @Override
  public String toString() {
    return "If " + condition.toString() + " Then";  // COV_NF_LINE
  }
}
