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
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.Error;
import com.google.devtools.simple.compiler.expressions.ExpressionValueUnused;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;

/**
 * This class implements statements consisting of an expression.
 * 
 * @author Herbert Czymontek
 */
public final class ExpressionStatement extends Statement {
  // Expression to evaluate
  private Expression expression;

  /**
   * Creates a new expression statement.
   *
   * @param position  source code start position of statement
   * @param expression  expression to evaluate
   */
  public ExpressionStatement(long position, Expression expression) {
    super(position);
    this.expression = expression;
  }

  @Override
  public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
    expression = expression.resolve(compiler, currentFunction);

    if (!expression.isAssignmentExpression() && !expression.isCallExpression()) {
      if (!expression.getType().isErrorType()) {
        compiler.error(getPosition(), Error.errAssignmentOrCallExprExpected);
      }
    }

    if (expression instanceof ExpressionValueUnused) {
      ((ExpressionValueUnused) expression).valueUnused(compiler);
    }
  }

  @Override
  public void generate(Method m) {
    generateLineNumberInformation(m);
    expression.generate(m);
  }

  /**
   * Returns the statement's expression.
   * 
   * @return  expression
   */
  public Expression getExpression() {
    return expression;
  }

  @Override
  public String toString() {
    return expression.toString();  // COV_NF_LINE
  }
}
