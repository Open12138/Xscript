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

package com.google.devtools.simple.compiler.symbols.synthetic;

import com.google.devtools.simple.classfiles.ClassFile;
import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.scanner.Scanner;
import com.google.devtools.simple.compiler.symbols.DataMemberSymbol;
import com.google.devtools.simple.compiler.symbols.EventHandlerSymbol;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.symbols.InstanceDataMemberSymbol;
import com.google.devtools.simple.compiler.symbols.ObjectSymbol;

/**
 * Synthetic symbol for a default constructor.
 * 
 * <p>All Simple objects are instantiated using the default constructor. After
 * that an Initialize event is triggered which allows for specific
 * initialization of the instances.
 * 
 * @author Herbert Czymontek
 */
public class ConstructorSymbol extends FunctionSymbol {

  /**
   * Creates a new default constructor symbol.
   * 
   * @param objectSymbol  object to be instantiated by the constructor
   */
  public ConstructorSymbol(ObjectSymbol objectSymbol) {
    super(Scanner.NO_POSITION, objectSymbol, "<init>");
  }

  @Override
  public boolean hasMeArgument() {
    return true;
  }

  @Override
  public void generate(Compiler compiler, ClassFile cf) {
    Method m = cf.newMethod(Method.ACC_PUBLIC, "<init>", getType().signature());

    m.startCodeGeneration();

    // Invoke superclass constructor
    m.generateInstrAload((short) 0);
    ObjectSymbol definingObject = getDefiningObject();
    m.generateInstrInvokespecial(definingObject.getBaseObjectInternalName(compiler),
        "<init>", "()V");

    // Initialize statically sized data members
    for (DataMemberSymbol dataMember : definingObject.getDataMembers()) {
      if (dataMember instanceof InstanceDataMemberSymbol) {
        dataMember.generateInitializer(m);
      }
    }

    // Register event handlers
    // Note that forms register event handlers in the synthetic $define() method. They also raise
    // their Initialize event after completing it.
    // TODO: forms shouldn't have special treatment for their Initialize events
    String internalName = definingObject.getType().internalName();
    if (definingObject.isForm()) {
      // Need to initialize properties before executing $define() method (otherwise property
      // initializers would overwrite changes made by $define()).
      m.generateInstrAload((short) 0);
      m.generateInstrInvokestatic(Compiler.RUNTIME_ROOT_INTERNAL + "/Objects",
          "initializeProperties", "(Ljava/lang/Object;)V");
      m.generateInstrAload((short) 0);
      m.generateInstrInvokevirtual(internalName, "$define", "()V");
    } else {
      for (EventHandlerSymbol eventHandler : definingObject.getEventHandlers()) {
        m.generateInstrAload((short) 0);
        m.generateInstrLdc(eventHandler.getEventTargetName());
        m.generateInstrLdc(eventHandler.getEventName());
        m.generateInstrInvokestatic(Compiler.RUNTIME_ROOT_INTERNAL + "/events/EventDispatcher",
            "registerEvent", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)V");
      }

      // Raise instance initialization event
      EventHandlerSymbol initializeEventHandler = 
          definingObject.getInstanceInitializeEventHandler();
      if (initializeEventHandler != null) {
        m.generateInstrAload((short) 0);
        m.generateInstrInvokevirtual(internalName, initializeEventHandler.getName(),
            initializeEventHandler.getType().signature());
      }
    }

    m.generateInstrReturn();
    m.finishCodeGeneration();
  }
}
