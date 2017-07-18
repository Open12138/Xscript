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

import com.google.devtools.simple.compiler.scanner.TokenKind;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.scanner.Scanner;
import com.google.devtools.simple.util.Preconditions;

/**
 * This class is the superclass for all statements.
 * 
 * @author Herbert Czymontek
 */
public abstract class Statement {

  // Statement block that this statement belongs to
  protected StatementBlock parentStatementBlock;

  // Source code start position of statement
  private final long position;

  /**
   * Creates a new statement.
   *
   * @param position  source code start position of statement
   */
  public Statement(long position) {
    this.position = position;
  }


  /**
   * Returns the source code position of the statement.
   * 
   * @return  source code start position of statement
   */
  public long getPosition() {
    return position;
  }

  /**
   * Set the statement block this statement belongs to.
   * 
   * @param parentStatementBlock  block this statement belongs to
   */
  final void setParentStatementBlock(StatementBlock parentStatementBlock) {
    this.parentStatementBlock = parentStatementBlock;
  }

  /**
   * Tries to find an exit label for the given statement kind. Statements
   * supporting Exit-statements need to override this method.
   * 
   * @param token  statement kind to look for (or {@link TokenKind#TOK_NONE}
   *               for next possible)
   * @return  exit label found or {@code null}
   */
  public Method.Label getExitLabel(TokenKind token) {
    Preconditions.checkNotNull(parentStatementBlock);

    return parentStatementBlock.getExitLabel(token);
  }

  /**
   * Resolves any identifiers or types involved in the expressions used by the
   * statement. After the resolution the statement is ready to have code
   * generated for it.
   * 
   * @param compiler  current compiler instance
   * @param currentFunction  current function being resolved
   *                         (can be {code null})
   */
  public abstract void resolve(Compiler compiler, FunctionSymbol currentFunction);

  /**
   * Generates code for the statement.
   * 
   * @param m  method to generate the code for
   */
  public abstract void generate(Method m);
  
  /**
   * Generates line number debug information for the statement.
   */
  protected void generateLineNumberInformation(Method m) {
    m.generateLineNumberInformation(Scanner.getLine(position));
  }
}
