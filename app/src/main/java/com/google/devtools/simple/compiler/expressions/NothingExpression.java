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
import com.google.devtools.simple.compiler.types.ObjectType;
import com.google.devtools.simple.compiler.types.Type;

/**
 * Implements the Nothing expression which will represents the default value
 * of which ever type it will resolve to.
 *
 * @author Herbert Czymontek
 */
public final class NothingExpression extends Expression {

  /**
   * Creates a new constant expression.
   * 
   * @param position  source code start position of expression
   */
  public NothingExpression(long position) {
    super(position);
  }

  /**
   * Changes the type of the expression.
   * 
   * @param toType  new type
   */
  public void changeType(Type toType) {
    type = toType;
  }
  
  @Override
  public Expression resolve(Compiler compiler, FunctionSymbol currentFunction) {
    type = ObjectType.objectType;
    return this;
  }

  @Override
  public void generate(Method m) {
    type.generateDefaultInitializationValue(m);
  }
}
