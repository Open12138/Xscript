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

package com.google.devtools.simple.compiler.scopes;

import com.google.devtools.simple.compiler.statements.synthetic.MarkerStatement;
import com.google.devtools.simple.compiler.symbols.LocalVariableSymbol;
import com.google.devtools.simple.compiler.symbols.Symbol;

/**
 * Scope for functions and statement blocks.
 * 
 * @author Herbert Czymontek
 */
public class LocalScope extends Scope {

  /**
   * Creates a new local scope (for a function or statement block).
   * 
   * @param outerScope  enclosing scope
   */
  public LocalScope(Scope outerScope) {
    super(outerScope);
  }

  /**
   * Marks the end-of-scope for all local variables defined in this scope.
   * 
   * @param marker  end-of-scope marker
   */
  public void markEndOfScope(MarkerStatement marker) {
    for (Symbol symbol : symbolMap.values()) {
      if (symbol instanceof LocalVariableSymbol) {
        ((LocalVariableSymbol) symbol).setEndScope(marker);
      }
    }
  }
}
