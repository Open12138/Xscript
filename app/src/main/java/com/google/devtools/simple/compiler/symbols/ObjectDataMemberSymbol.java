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
import com.google.devtools.simple.classfiles.Field;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.RuntimeLoader;
import com.google.devtools.simple.compiler.types.Type;

/**
 * Symbol used for object data members.
 * 
 * @author Herbert Czymontek
 */
public class ObjectDataMemberSymbol extends DataMemberSymbol {

  /**
   * Creates a new object data member symbol (the equivalent of a Java static
   * field).
   * 
   * @param position  source code start position of symbol 
   * @param objectSymbol  defining object
   * @param name  data member name
   * @param type  data member type
   */
  public ObjectDataMemberSymbol(long position, ObjectSymbol objectSymbol, String name, Type type) {
    super(position, objectSymbol, name, type);
  }

  @Override
  public void generateRead(Method m) {
    m.generateInstrGetstatic(getDefiningObject().getType().internalName(), getName(),
        getType().signature());
  }

  @Override
  public void generateWrite(Method m) {
    m.generateInstrPutstatic(getDefiningObject().getType().internalName(), getName(),
        getType().signature());
  }

  @Override
  public void generate(ClassFile cf) {
    cf.newField((short)(Field.ACC_PUBLIC|Field.ACC_STATIC), getName(),
        getType().signature()).getRuntimeVisibleAnnotationsAttribute().newAnnotation(
            'L' + RuntimeLoader.ANNOTATION_INTERNAL + "/SimpleDataElement;");
  }

  @Override
  public String toString() {
    return "Static Dim " + super.toString();  // COV_NF_LINE
  }
}
