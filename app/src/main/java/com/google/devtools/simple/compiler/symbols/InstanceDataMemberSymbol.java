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
import com.google.devtools.simple.compiler.RuntimeLoader;
import com.google.devtools.simple.compiler.expressions.Expression;
import com.google.devtools.simple.compiler.types.Type;
import com.google.devtools.simple.classfiles.Field;
import com.google.devtools.simple.classfiles.Method;

import java.util.List;

/**
 * Symbol used for instance data members.
 * 
 * @author Herbert Czymontek
 */
public final class InstanceDataMemberSymbol extends DataMemberSymbol implements InstanceMember {

  /**
   * Creates a new instance data member.
   * 
   * @param position  source code start position of symbol 
   * @param objectSymbol  defining object
   * @param name  data member name
   * @param type  data member type
   */
  public InstanceDataMemberSymbol(long position, ObjectSymbol objectSymbol, String name,
      Type type) {
    super(position, objectSymbol, name, type);
  }

  @Override
  public void generateRead(Method m) {
    m.generateInstrGetfield(getDefiningObject().getType().internalName(),
        getName(), getType().signature());
  }

  @Override
  public void generateWrite(Method m) {
    m.generateInstrPutfield(getDefiningObject().getType().internalName(),
        getName(), getType().signature());
  }

  @Override
  public void generate(ClassFile cf) {
    cf.newField(Field.ACC_PUBLIC, getName(), 
        getType().signature()).getRuntimeVisibleAnnotationsAttribute().newAnnotation(
            'L' + RuntimeLoader.ANNOTATION_INTERNAL + "/SimpleDataElement;");
  }

  @Override
  protected void generateInitializer(Method m, List<Expression> dimensions) {
    m.generateInstrAload((short) 0);
    super.generateInitializer(m, dimensions);
  }

  @Override
  public String toString() {
    return "Dim " + super.toString();  // COV_NF_LINE
  }
}
