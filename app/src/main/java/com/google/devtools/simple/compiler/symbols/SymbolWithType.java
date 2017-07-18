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

package com.google.devtools.simple.compiler.symbols;

import com.google.devtools.simple.compiler.types.Type;

/**
 * Symbols having a type associated with them, need to implement this
 * interface.
 * 
 * @author Herbert Czymontek
 */
public interface SymbolWithType {
  /**
   * Returns the type of the symbol.
   * 
   * @return  symbol type
   */
  Type getType();

  /**
   * Sets the type for the symbol.
   * 
   * @param type  type for symbol
   */
  void setType(Type type);
}
