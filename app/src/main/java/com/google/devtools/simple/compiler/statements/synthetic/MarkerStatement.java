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

import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.scanner.Scanner;
import com.google.devtools.simple.compiler.statements.Statement;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.classfiles.Method.Label;

/**
 * A synthetic statement marking a code location.
 * 
 * <p>This is, for example,  used for generation of local variable debug
 * information.
 * 
 * @author Herbert Czymontek
 */
public class MarkerStatement extends Statement {

  // Label used for marking
  Label marker;

  /**
   * Creates a new marker statement.
   */
  public MarkerStatement() {
    super(Scanner.NO_POSITION);

    marker = Method.newLabel();
  }

  @Override
  public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
  }

  @Override
  public void generate(Method m) {
    m.setLabel(marker);
  }

  /**
   * Returns marker label.
   * 
   * @return  marker label
   */
  public Label getMarker() {
    return marker;
  }
}
