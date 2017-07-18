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
import com.google.devtools.simple.compiler.symbols.EventHandlerSymbol;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.symbols.ObjectSymbol;

/**
 * Raises an initialization event for the given data member.
 * 
 * @author Herbert Czymontek
 */
public final class RegisterEventHandlersStatement extends Statement {

  // Object registering event handlers
  private final ObjectSymbol object;

  /**
   * Creates a new event handler initialization statement.
   *
   * @param object  object registering event handlers
   */
  public RegisterEventHandlersStatement(ObjectSymbol object) {
    super(Scanner.NO_POSITION);

    this.object = object;
  }

  @Override
  public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
    // Nothing to resolve
  }

  @Override
  public void generate(Method m) {
    for (EventHandlerSymbol eventHandler : object.getEventHandlers()) {
      m.generateInstrAload((short) 0);
      m.generateInstrLdc(eventHandler.getEventTargetName());
      m.generateInstrLdc(eventHandler.getEventName());
      m.generateInstrInvokestatic(Compiler.RUNTIME_ROOT_INTERNAL + "/events/EventDispatcher",
          "registerEvent", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)V");
    }
  }
}
