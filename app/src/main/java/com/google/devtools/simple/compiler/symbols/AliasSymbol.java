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

import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.classfiles.Method;

/**
 * Symbol used as an alias for another symbol.
 * 
 * @author Herbert Czymontek
 */
public final class AliasSymbol extends Symbol {

  // Actual symbol the alias refers to
  private final Symbol actualSymbol;

  /**
   * Creates a new alias symbol.
   * 
   * @param position  source code start position of symbol 
   * @param name  name of alias
   * @param actualSymbol  symbol alias refers to
   */
  public AliasSymbol(long position, String name, Symbol actualSymbol) {
    super(position, name);
    this.actualSymbol = actualSymbol;
  }

  @Override
  public Symbol getActualSymbol() {
    return actualSymbol;
  }

  @Override
  public void generateRead(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  @Override
  public void generateWrite(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }
}
