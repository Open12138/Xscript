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

import com.google.devtools.simple.compiler.symbols.ObjectSymbol;
import com.google.devtools.simple.compiler.symbols.Symbol;
import com.google.devtools.simple.compiler.types.ObjectType;

/**
 * Scope for objects.
 * 
 * @author Herbert Czymontek
 */
public final class ObjectScope extends Scope {
  // Corresponding object symbol for this scope
  private ObjectSymbol objectSymbol;

  /**
   * Creates a new object scope.
   * 
   * @param objectSymbol  corresponding object symbol
   * @param outerScope  enclosing scope
   */
  public ObjectScope(ObjectSymbol objectSymbol, Scope outerScope) {
    super(outerScope);
    this.objectSymbol = objectSymbol;
  }

  /**
   * Looks up the identifier first in this object and if cannot be found in any
   * base object scope.
   * 
   * @param identifier  identifier to look up
   * @return  symbol found or {@code null}
   */
  public Symbol lookupInObject(String identifier) {
    ObjectType objectType = (ObjectType)objectSymbol.getType();
    do {
      Symbol symbol =
          objectType.getObjectSymbol().getScope().lookupShallow(identifier);
      if (symbol != null) {
        return symbol;
      }

      objectType = objectType.getObjectSymbol().getBaseObject();
    } while (objectType != null);

    return null;
  }

  @Override
  public Symbol lookupDeep(String identifier) {
    Symbol symbol = lookupInObject(identifier);
    if (symbol != null) {
      return symbol;
    }

    return outerScope.lookupDeep(identifier);
  }
}
