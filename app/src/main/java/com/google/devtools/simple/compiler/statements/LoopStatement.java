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
import com.google.devtools.simple.compiler.scanner.TokenKind;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.classfiles.Method;

/**
 * This is the superclass for all loop-related statements.
 * 
 * @author Herbert Czymontek
 */
public abstract class LoopStatement extends Statement {
  // Statements in the body of the loop
  protected final StatementBlock loopStatements;

  // Label for 'Exit'-statements to branch to
  protected final Method.Label exitLabel;

  /**
   * Creates a new loop statement.
   *
   * @param position  source code start position of statement
   * @param loopStatements  statements in loop body
   */
  public LoopStatement(long position, StatementBlock loopStatements) {
    super(position);
    this.loopStatements = loopStatements;
    exitLabel = Method.newLabel();
  }

  @Override
  public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
    loopStatements.resolve(compiler, currentFunction, this);
  }

  /**
   * Returns the token that syntactically starts the loop (which is the same
   * token to used in an 'Exit'-statement to break out of the loop).
   * 
   * @return  token syntactically starting loop 
   */
  protected abstract TokenKind getLoopStartToken();
  
  @Override
  public final Method.Label getExitLabel(TokenKind token) {
    if (token == TokenKind.TOK_NONE || token == getLoopStartToken()) {
      return exitLabel;
    } else {
      return parentStatementBlock.getExitLabel(token);
    }
  }
}
