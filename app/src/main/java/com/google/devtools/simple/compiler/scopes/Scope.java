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

import com.google.devtools.simple.compiler.symbols.Symbol;

import java.util.HashMap;

/**
 * Superclass of all scope implementations.
 * 
 * @author Herbert Czymontek
 */
public abstract class Scope {

  // Maps identifiers to symbols
  protected final HashMap<String, Symbol> symbolMap;

  // Enclosing scope
  protected final Scope outerScope;

  /**
   * Creates a new scope.
   * 
   * @param outerScope  enclosing scope
   */
  public Scope(Scope outerScope) {
    this.outerScope = outerScope;

    symbolMap = new HashMap<String, Symbol>();
  }

  /**
   * Adds a symbol to the scope.
   * 
   * @param symbol  symbol to be added
   */
  public void enterSymbol(Symbol symbol) {
    symbolMap.put(symbol.getName(), symbol);
  }

  /**
   * Removes a symbol from the scope
   * 
   * @param symbol  symbol to remove
   */
  public void removeSymbol(Symbol symbol) {
    symbolMap.remove(symbol.getName());
  }

  /**
   * Looks up the identifier just in this scope.
   * 
   * @param identifier  identifier to look up
   * @return  symbol found or {@code null}
   */
  public Symbol lookupShallow(String identifier) {
    return symbolMap.get(identifier);
  }

  /**
   * Looks up the identifier first in this scope and if it cannot be found in
   * any enclosing scope.
   * 
   * @param identifier  identifier to look up
   * @return  symbol found or {@code null}
   */
  public Symbol lookupDeep(String identifier) {
    Symbol symbol = lookupShallow(identifier);
    if (symbol != null) {
      return symbol;
    }
    return outerScope.lookupDeep(identifier);
  }
}
