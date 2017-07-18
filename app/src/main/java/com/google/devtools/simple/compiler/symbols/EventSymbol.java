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

import com.google.devtools.simple.classfiles.ClassFile;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.RuntimeLoader;
import com.google.devtools.simple.compiler.types.FunctionType;
import com.google.devtools.simple.compiler.types.Type;

import java.util.List;

/**
 * Symbol used for defining events.
 * 
 * @author Herbert Czymontek
 */
public final class EventSymbol extends InstanceFunctionSymbol {

  /**
   * Creates a new event symbol.
   * 
   * @param position  source code start position of symbol 
   * @param definingObject  defining object
   * @param name  event name
   */
  public EventSymbol(long position, ObjectSymbol definingObject, String name) {
    super(position, definingObject, name);
  }

  @Override
  public void generate(Compiler compiler, ClassFile cf) {
    Method m = cf.newMethod(Method.ACC_PUBLIC, getName(), getType().signature());

    m.getRuntimeVisibleAnnotationsAttribute().newAnnotation(
        'L' + RuntimeLoader.ANNOTATION_INTERNAL + "/SimpleEvent;");

    m.startCodeGeneration();
    m.generateInstrAload((short) 0);
    m.generateInstrLdc(getName());

    List<Type> formalArgList = ((FunctionType) getType()).getFormalArgumentList();
    int formalArgCount = formalArgList.size();
    m.generateInstrLdc(formalArgCount);
    m.generateInstrAnewarray("java/lang/Object");
    for (int i = 0; i < formalArgCount; i++) {
      m.generateInstrDup();
      m.generateInstrLdc(i);
      Type formalArgType = formalArgList.get(i);
      formalArgType.generateLoadLocal(m, (short) (i + 1));
      formalArgType.generateJavaBox(m);
      m.generateInstrAastore();
    }

    m.generateInstrInvokestatic(Compiler.RUNTIME_ROOT_INTERNAL + "/events/EventDispatcher",
        "dispatchEvent", "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)V");
    
    m.generateInstrReturn();

    m.finishCodeGeneration();
  }
}
