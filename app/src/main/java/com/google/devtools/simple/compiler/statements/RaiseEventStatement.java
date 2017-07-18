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

package com.google.devtools.simple.compiler.statements;

import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.Error;
import com.google.devtools.simple.compiler.expressions.Expression;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.compiler.symbols.InstanceFunctionSymbol;
import com.google.devtools.simple.compiler.types.FunctionType;
import com.google.devtools.simple.compiler.types.Type;
import com.google.devtools.simple.util.Strings;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.symbols.EventSymbol;
import com.google.devtools.simple.compiler.symbols.ObjectSymbol;

import java.util.List;

/**
 * Implements the RaiseEvent statement.
 * 
 * @author Herbert Czymontek
 */
public final class RaiseEventStatement extends Statement {

  // Event to raise
  private final String eventName;

  // Resolved event
  private EventSymbol event;
  
  // Arguments for event handler
  private final List<Expression> actualArgList;

  /**
   * Creates a new expression statement.
   *
   * @param position  source code start position of statement
   * @param eventName  event to raise
   * @param actualArgList  actual arguments for the event handler
   */
  public RaiseEventStatement(long position, String eventName, List<Expression> actualArgList) {
    super(position);
    this.eventName = eventName;
    this.actualArgList = actualArgList;
  }

  @Override
  public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
    // Resolve event
    ObjectSymbol definingObject = currentFunction.getDefiningObject();
    event = (EventSymbol) definingObject.getScope().lookupInObject(eventName);
    if (event == null) {
      compiler.error(getPosition(), Error.errUndefinedSymbol, eventName);
      return;
    }

    // Current function must be instance function
    if (!(currentFunction instanceof InstanceFunctionSymbol)) {
      compiler.error(getPosition(), Error.errRaiseEventFromObjectFunction);
    }

    // Check argument counts of formal and actual argument list
    FunctionType funcType = (FunctionType) event.getType();
    List<Type> formalArgList = funcType.getFormalArgumentList();

    int actualArgCount = actualArgList.size();
    int formalArgCount = formalArgList.size();
    if (actualArgCount != formalArgCount) {
      compiler.error(getPosition(),
          actualArgCount > formalArgCount ? Error.errTooManyArguments : Error.errTooFewArguments);
    }

    // Resolve arguments
    for (int i = 0; i < actualArgCount; i++) {
      // If the actual argument type is not exactly the formal argument type
      // then try to find a conversion from actual to formal argument type.
      Expression arg = actualArgList.get(i).resolve(compiler, currentFunction);

      if (i < formalArgCount) {
        // We only do this so that we don't crash in the case of too many arguments
        arg = arg.checkType(compiler, formalArgList.get(i));
      }

      actualArgList.set(i, arg);
    }
  }

  @Override
  public void generate(Method m) {
    generateLineNumberInformation(m);

    m.generateInstrAload((short) 0);
    for (Expression actualArg : actualArgList) {
      actualArg.generate(m);
    }
    m.generateInstrInvokevirtual(event.getDefiningObject().getType().internalName(),
        event.getName(), event.getType().signature());
  }

  @Override
  public String toString() {
    return "RaiseEvent " + eventName + '(' + Strings.join(", ", actualArgList) + ')';  // COV_NF_LINE
  }
}
