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
import com.google.devtools.simple.compiler.scanner.TokenKind;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.expressions.ConstantBooleanExpression;
import com.google.devtools.simple.compiler.expressions.ConstantExpression;

/**
 * This class implements the Do-While-Statement that loops as long as the loop
 * condition is {@code true}.
 * 
 * @author Herbert Czymontek
 */
public final class DoWhileStatement extends ConditionalLoopStatement {

  /**
   * Creates a new Do-While-statement.
   *
   * @param position  source code start position of statement
   * @param condition  loop condition
   * @param loopStatements  statements in loop body
   */
  public DoWhileStatement(long position, Expression condition, StatementBlock loopStatements) {
    super(position, condition, loopStatements);
  }

  @Override
  protected TokenKind getLoopStartToken() {
    return TokenKind.TOK_DO;
  }

  @Override
  public void generate(Method m) {
    // Optimize code generation for constant loop conditions
     if (condition instanceof ConstantExpression) {
      if (((ConstantBooleanExpression)condition).getBoolean()) {
        // Infinite loop (unless there is an Exit-Statement somewhere)
        Method.Label loopLabel = Method.newLabel();
        m.setLabel(loopLabel);
        loopStatements.generate(m);
        generateLineNumberInformation(m);
        m.generateInstrGoto(loopLabel);
      } else {
        // Execute loop just once
        loopStatements.generate(m);
      }
    } else {
      // Regular loop
      Method.Label loopLabel = Method.newLabel();
      m.setLabel(loopLabel);
      loopStatements.generate(m);
      generateLineNumberInformation(m);
      condition.generateBranchOnTrue(m, loopLabel);
    }

    m.setLabel(exitLabel);
  }

  @Override
  public String toString() {
    return "Do ... While " + condition.toString();  // COV_NF_LINE
  }
}
