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
import com.google.devtools.simple.compiler.scopes.LocalScope;
import com.google.devtools.simple.compiler.scopes.Scope;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.statements.synthetic.MarkerStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a statement block.
 * 
 * @author Herbert Czymontek
 */
public final class StatementBlock {

  // Statements in this statement block
  private final List<Statement> statements;

  // Local scope for this statement block
  private final LocalScope scope;

  // Statement owning this statement block 
  protected Statement parentStatement;

  /**
   * Creates a new statement block.
   * 
   * @param outerScope  enclosing scope
   */
  public StatementBlock(Scope outerScope) {
    statements = new ArrayList<Statement>();
    scope = new LocalScope(outerScope);
  }

  /**
   * Adds a statement to the statement block.
   * 
   * @param statement  statement to add
   */
  public void add(Statement statement) {
    statements.add(statement);
    statement.setParentStatementBlock(this);
  }

  /**
   * Returns the local scope of the statement block
   * 
   * @return  local statement block scope
   */
  public Scope getScope() {
    return scope;
  }

  /**
   * Tries to find an exit label for the given statement kind.
   * 
   * @param token  statement kind to look for (or {@link TokenKind#TOK_NONE}
   *               for next possible)
   * @return  exit label found or {@code null}
   */
  public Method.Label getExitLabel(TokenKind token) {
    return parentStatement != null ? parentStatement.getExitLabel(token) : null;
  }

  /**
   * Resolves any identifiers or types involved in the expressions used by the
   * statements in this block. After the resolution the statements are ready to
   * have code generated for them.
   * 
   * @param compiler  current compiler instance
   * @param currentFunction  current function being resolved
   *                         (can be {code null})
   * @param parent  parent statement of this statement block
   */
  public void resolve(Compiler compiler, FunctionSymbol currentFunction, Statement parent) {
    this.parentStatement = parent;

    // Add a marker statement to be used by the local scope statement block scope to mark the
    // end-of-scope of all local variables defined in that scope
    MarkerStatement marker = new MarkerStatement();
    add(marker);
    scope.markEndOfScope(marker);

    // Resolve individual statements
    for (Statement stmt : statements) {
      stmt.resolve(compiler, currentFunction);
    }
  }

  /**
   * Generates code for the statements.
   * 
   * @param m  method to generate the code for
   */
  public void generate(Method m) {
    for (Statement stmt : statements) {
      stmt.generate(m);
    }
  }
}
