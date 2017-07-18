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
import com.google.devtools.simple.compiler.expressions.Expression;
import com.google.devtools.simple.compiler.types.StringType;
import com.google.devtools.simple.compiler.types.Type;

import java.util.List;

/**
 * This is the superclass for all data member symbols.
 * 
 * @author Herbert Czymontek
 */
public abstract class DataMemberSymbol extends VariableSymbol {

  // Object defining the data member
  private final ObjectSymbol definingObject;

  /**
   * Creates a new data member symbol.
   * 
   * @param position  source code start position of symbol 
   * @param objectSymbol  defining object
   * @param name  data member name
   * @param type  data member type
   */
  public DataMemberSymbol(long position, ObjectSymbol objectSymbol, String name, Type type) {
    super(position, name, type);
    this.definingObject = objectSymbol;
  }

  /**
   * Returns the object defining the data member.
   * 
   * @return  defining object
   */
  public ObjectSymbol getDefiningObject() {
    return definingObject;
  }

  /**
   * Generates an entry for the data member in the classfile.
   * 
   * @param cf  class file
   */
  public abstract void generate(ClassFile cf);

  @Override
  protected void generateInitializer(Method m, List<Expression> dimensions) {
    if (dimensions != null) {
      for (Expression dimension : dimensions) {
        dimension.generate(m);
      }
      getType().generateAllocateArray(m);
      generateWrite(m);
    } else if (getType() == StringType.stringType) {
      getType().generateDefaultInitializationValue(m);
      generateWrite(m);
    }
  }
}
