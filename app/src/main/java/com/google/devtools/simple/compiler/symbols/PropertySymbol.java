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

import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.types.Type;

/**
 * Symbol used for properties.
 * 
 * @author Herbert Czymontek
 */
public final class PropertySymbol extends Symbol implements InstanceMember, SymbolWithType {

  // Object defining the property
  private ObjectSymbol definingObject;

  // Property type
  private Type type;

  /**
   * Creates a new property symbol.
   * 
   * @param position  source code start position of symbol 
   * @param objectSymbol  defining object
   * @param name  property name
   * @param type  property type
   */
  public PropertySymbol(long position, ObjectSymbol objectSymbol, String name, Type type) {
    super(position, name);
    this.definingObject = objectSymbol;
    this.type = type;
  }

  @Override
  public void generateRead(Method m) {
    generateInstrInvoke(m, getDefiningObject().getType().internalName(), getName(),
        "()" + type.signature());
  }

  @Override
  public void generateWrite(Method m) {
    generateInstrInvoke(m, getDefiningObject().getType().internalName(), getName(),
        "(" + type.signature() + ")V");
  }

  private void generateInstrInvoke(Method m, String internalName, String name, String signature) {
    if (definingObject.isInterface()) {
      m.generateInstrInvokeinterface(internalName, name, signature);
    } else {
      m.generateInstrInvokevirtual(internalName, name, signature);
    }
  }

  // InstanceMember implementation

  @Override
  public ObjectSymbol getDefiningObject() {
    return definingObject;
  }

  // SymbolWithType implementation

  @Override
  public final Type getType() {
    return type;
  }

  @Override
  public final void setType(Type type) {
    this.type = type;
  }
}
