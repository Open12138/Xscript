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
import com.google.devtools.simple.compiler.symbols.DataMemberSymbol;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.compiler.symbols.InstanceDataMemberSymbol;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.symbols.ObjectSymbol;
import com.google.devtools.simple.compiler.types.ObjectType;

/**
 * Raises an initialization event for the given data member.
 * 
 * @author Herbert Czymontek
 */
public final class RaiseInitializeEventStatement extends Statement {

  // Data member to raise initialization event for
  private final DataMemberSymbol dataMember;

  /**
   * Creates a new initialization event statement.
   *
   * @param dataMember  data member to raise initialization event for
   */
  public RaiseInitializeEventStatement(DataMemberSymbol dataMember) {
    super(Scanner.NO_POSITION);

    this.dataMember = dataMember;
  }

  @Override
  public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
    // Nothing to resolve
  }

  @Override
  public void generate(Method m) {
    // Never need to do this for Forms. This is already handled in the runtime library.
    ObjectType type = (ObjectType) dataMember.getType();
    ObjectSymbol symbol = type.getObjectSymbol();
    if (!symbol.isForm()) {
      if (dataMember instanceof InstanceDataMemberSymbol) {
        m.generateInstrAload((short) 0);
      }
      dataMember.generateRead(m);

      if (symbol.isInterface()) {
        m.generateInstrInvokeinterface(type.internalName(), "Initialize", "()V");
      } else {
        m.generateInstrInvokevirtual(type.internalName(), "Initialize", "()V");
      }
    }
  }
}
