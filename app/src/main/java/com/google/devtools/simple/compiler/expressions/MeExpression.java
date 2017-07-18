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

package com.google.devtools.simple.compiler.expressions;

import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.scopes.Scope;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.symbols.ObjectSymbol;
import com.google.devtools.simple.compiler.symbols.Symbol;
import com.google.devtools.simple.compiler.types.ObjectType;
import com.google.devtools.simple.compiler.types.Type;

/**
 * This class represents the 'Me' identifier (the equivalent of 'this' in
 * Java).
 * 
 * @author Herbert Czymontek
 */
public final class MeExpression extends IdentifierExpression {

  // For inner class Me expressions this is the outer class
  private ObjectSymbol enclosingObject;

  /**
   * Creates a new Me expression
   *
   * @param position  source code start position of expression
   * @param objectSymbol  object type of Me
   */
  public MeExpression(long position, ObjectSymbol objectSymbol) {
    super(position, objectSymbol.getScope(), null, "Me");

    type = objectSymbol.getType();
  }

  /**
   * Sets an outer class for the Me expression.
   * 
   * @param enclosingObject  outer class
   */
  public void setOuterClass(ObjectSymbol enclosingObject) {
    this.enclosingObject = enclosingObject;
  }

  @Override
  public Symbol getResolvedIdentifier() {
    assert false;
    return null;
  }

  @Override
  public Scope getScope() {
    return ((ObjectType) type).getScope();
  }

  @Override
  public Expression resolve(Compiler compiler, FunctionSymbol currentFunction) {
    // Loaded types aren't resolved explicitly. This will make sure that they are getting resolved
    // lazily.
    type.resolve(compiler);

    return this;
  }

  @Override
  public void generate(Method m) {
    m.generateInstrAload((short) 0);
    if (enclosingObject != null) {
      Type enclosingObjectType = enclosingObject.getType();
      m.generateInstrGetfield(type.internalName(), "Me$0", enclosingObjectType.signature());
    }
  }

  @Override
  public void generatePrepareWrite(Method m) {
    m.generateInstrAload((short) 0);
    if (enclosingObject != null) {
      Type enclosingObjectType = enclosingObject.getType();
      m.generateInstrGetfield(type.internalName(), "Me$0", enclosingObjectType.signature());
    }
  }

  @Override
  public void generateWrite(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  @Override
  public String toString() {
    return "Me";  // COV_NF_LINE
  }
}
