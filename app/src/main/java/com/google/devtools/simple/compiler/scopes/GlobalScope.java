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

/**
 * Global scope (which is the outermost scope for all other scopes).
 * 
 * @author Herbert Czymontek
 */
public final class GlobalScope extends Scope {

  /**
   * Creates the global scope.
   */
  public GlobalScope() {
    super(null);
  }

  @Override
  public final Symbol lookupShallow(String identifier) {
    // Only the global scope can contain Alias symbols. Therefore we need to invoke the
    // getActualSymbol() method to only return actual symbols!
    Symbol symbol = super.lookupShallow(identifier);
    return symbol != null ? symbol.getActualSymbol() : null;
  }

  @Override
  public Symbol lookupDeep(String identifier) {
    return lookupShallow(identifier);
  }
}
