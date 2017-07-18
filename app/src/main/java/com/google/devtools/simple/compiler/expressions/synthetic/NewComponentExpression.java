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

package com.google.devtools.simple.compiler.expressions.synthetic;

import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.expressions.Expression;
import com.google.devtools.simple.compiler.scanner.Scanner;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.compiler.types.Type;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.types.ObjectType;

/**
 * This class is used for component instantiation expressions.
 * 
 * @author Herbert Czymontek
 */
public final class NewComponentExpression extends Expression {

  // Container of component
  private Expression container;

  // Internal name of component implementation
  private String internalName;

  /**
   * Creates a new component instantiation expression.
   *
   * @param container  container in which component resides
   * @param type  component type to instantiate
   */
  public NewComponentExpression(Expression container, Type type) {
    super(Scanner.NO_POSITION);

    this.type = type;
    this.container = container;
  }

  @Override
  public Expression resolve(Compiler compiler, FunctionSymbol currentFunction) {
    type.resolve(compiler);

    container = container.resolve(compiler, currentFunction);

    internalName = compiler.getComponentImplementationInternalName((ObjectType) type);

    return this;
  }

  @Override
  public void generate(Method m) {
    m.generateInstrNew(internalName);
    m.generateInstrDup();
    container.generate(m);
    m.generateInstrInvokespecial(internalName, "<init>",
        "(L" + Compiler.RUNTIME_ROOT_INTERNAL + "/components/ComponentContainer;)V");
    m.generateInstrDup();
    m.generateInstrInvokestatic(Compiler.RUNTIME_ROOT_INTERNAL + "/Objects",
        "initializeProperties", "(Ljava/lang/Object;)V");
  }

  @Override
  public String toString() {
    return "New " + type.toString() + " On " + container.toString();
  }
}
