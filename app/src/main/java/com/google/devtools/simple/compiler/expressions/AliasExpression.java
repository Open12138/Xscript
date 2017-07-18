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

package com.google.devtools.simple.compiler.expressions;

import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.symbols.AliasSymbol;

/**
 * This class represents an alias expression which defines an alternate name
 * for a symbol.
 *
 * @author Herbert Czymontek
 */
public final class AliasExpression extends Expression {
  // New alternate name
  private final String aliasName;

  // Expression for original name
  private final IdentifierExpression aliasExpr;

  // Symbol for alternate name
  private AliasSymbol aliasSymbol;

  /**
   * Creates a new alias expression
   * 
   * @param position  source code start position of expression
   * @param aliasName  alternate name
   * @param aliasExpr  expression for original name
   */
  public AliasExpression(long position, String aliasName, IdentifierExpression aliasExpr) {
    super(position);

    this.aliasName = aliasName;
    this.aliasExpr = aliasExpr;
  }

  /**
   * Returns symbol for alternate name.
   * 
   * @return  alternate name symbol
   */
  public AliasSymbol getAliasSymbol() {
    return aliasSymbol;
  }

  @Override
  public Expression resolve(Compiler compiler, FunctionSymbol currentFunction) {
    aliasExpr.resolve(compiler, currentFunction);
    aliasSymbol = new AliasSymbol(getPosition(), aliasName, aliasExpr.getResolvedIdentifier());

    return this;
  }

  @Override
  public void generate(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }
}
