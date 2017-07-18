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

/**
 * This is the superclass for expressions with a single operand.
 * 
 * @author Herbert Czymontek
 */
public abstract class UnaryExpression extends Expression {

  // Single operand
  protected Expression operand;

  /**
   * Creates an expression with a single operand.
   * 
   * @param position  source code start position of expression
   * @param operand  single operand
   */
  public UnaryExpression(long position, Expression operand) {
    super(position);

    this.operand = operand;
  }

  @Override
  public Expression resolve(Compiler compiler, FunctionSymbol currentFunction) {
    operand.resolve(compiler, currentFunction);
    return this;
  }

  @Override
  public void generate(Method m) {
    operand.generate(m);
  }
}
