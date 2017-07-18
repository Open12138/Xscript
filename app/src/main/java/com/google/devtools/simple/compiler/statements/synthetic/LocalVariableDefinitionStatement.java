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

package com.google.devtools.simple.compiler.statements.synthetic;

import com.google.devtools.simple.compiler.symbols.LocalVariableSymbol;
import com.google.devtools.simple.classfiles.Method;

/**
 * A synthetic statement for marking the definition of a local variable.
 * 
 * @author Herbert Czymontek
 */
public final class LocalVariableDefinitionStatement extends MarkerStatement {

  // Local variable
  private final LocalVariableSymbol local;
  
  /**
   * Creates a local variable definition statement.
   *
   * @param local  associated local variable
   */
  public LocalVariableDefinitionStatement(LocalVariableSymbol local) {
    this.local = local;
  }

  @Override
  public void generate(Method m) {
    local.generateInitializer(m);

    super.generate(m);
  }
}
