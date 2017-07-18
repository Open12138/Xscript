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
import com.google.devtools.simple.compiler.scanner.TokenKind;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.classfiles.Method;

/**
 * This class implements the Exit-statement.
 * 
 * @author Herbert Czymontek
 */
public final class ExitStatement extends Statement {
  // Found exit label
  private Method.Label exitLabel;

  // Token describing statement kind to look for (or TOK_NONE for next possible)
  private final TokenKind token;

  /**
   * Creates a new Exit-statement.
   *
   * @param position  source code start position of statement
   * @param token  statement kind to look for (or TOK_NONE for next possible)
   */
  public ExitStatement(long position, TokenKind token) {
    super(position);
    this.token = token;
  }

  @Override
  public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
    // Use exit token to find appropriate exit label
    switch (token) {
      default:
        Compiler.internalError();  // COV_NF_LINE
        break;
  
      case TOK_NONE:
        // Try to exit next possible statement
        exitLabel = parentStatementBlock.getExitLabel(token);
        if (exitLabel == null) {
          exitLabel = currentFunction.getExitLabel();
        }
        break;
  
      case TOK_DO:
      case TOK_FOR:
      case TOK_WHILE:
        exitLabel = parentStatementBlock.getExitLabel(token);
        break;
  
      case TOK_EVENT:
      case TOK_FUNCTION:
      case TOK_PROPERTY:
      case TOK_SUB:
        if (token == currentFunction.getExitToken()) {
          exitLabel = currentFunction.getExitLabel();
        }
        break;
    }

    // No applicable exit label found, report an error
    if (exitLabel == null) {
      compiler.error(getPosition(), Error.errNoMatchForExit, token.toString());
    }
  }

  @Override
  public void generate(Method m) {
    generateLineNumberInformation(m);
    m.generateInstrGoto(exitLabel);
  }

  @Override
  public String toString() {
    return "Exit";  // COV_NF_LINE
  }
}
