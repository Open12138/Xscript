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

/**
 * Symbol used for instance functions and procedures.
 * 
 * @author Herbert Czymontek
 */
public class InstanceFunctionSymbol extends FunctionSymbol implements InstanceMember {

  /**
   * Creates a new instance function symbol.
   *
   * @param position  source code start position of symbol 
   * @param objectSymbol  defining object
   * @param name  function name
   */
  public InstanceFunctionSymbol(long position, ObjectSymbol objectSymbol, String name) {
    super(position, objectSymbol, name);
  }

  @Override
  public boolean hasMeArgument() {
    return true;
  }
}
