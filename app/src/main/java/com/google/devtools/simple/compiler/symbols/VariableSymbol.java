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

import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.expressions.Expression;
import com.google.devtools.simple.compiler.types.IntegerType;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.types.ArrayType;
import com.google.devtools.simple.compiler.types.Type;
import com.google.devtools.simple.util.Preconditions;

import java.util.List;

/**
 * Superclass for all variable symbols.
 * 
 * @author Herbert Czymontek
 */
public abstract class VariableSymbol extends Symbol implements SymbolWithType {

  // Type of variable
  private Type type;

  // Static array dimensions for array types (otherwise null) 
  private List<Expression> dimensions;

  /**
   * Creates a new variable symbol.
   * 
   * @param position  source code start position of symbol 
   * @param name  variable name
   * @param type  variable type
   */
  public VariableSymbol(long position, String name, Type type) {
    super(position, name);
    this.type = type;
  }

  public final Type getType() {
    return type;
  }

  public final void setType(Type type) {
    this.type = type;
  }

  @Override
  public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
    if (dimensions != null) {
      for (int i = 0; i < dimensions.size(); i++) {
        dimensions.set(i,
            dimensions.get(i).resolve(compiler, currentFunction).checkType(compiler,
                IntegerType.integerType));
      }
    }
  }

  /**
   * Sets the dimension expressions for a statically sized array.
   *
   * @param dimensions  static array dimensions for array types
   *                    (can be {@code null})
   */
  public final void setStaticArrayDimensions(List<Expression> dimensions) {
    if (dimensions != null) {
    Preconditions.checkArgument(type.isArrayType() &&
        ((ArrayType) type).getDimensions() == dimensions.size());

      // Only interested in static dimensions
      if (dimensions.get(0) != null) {
        this.dimensions = dimensions;
      }
    }
  }

  /**
   * Generates code to initialize a variable.
   * 
   * @param m  method to generate the code for
   */
  public final void generateInitializer(Method m) {
    generateInitializer(m, dimensions);
  }

  /**
   * Generates code to initialize a variable.
   * 
   * @param m  method to generate the code for
   * @param dims  static array dimensions for array types or {@code null}
   */
  protected abstract void generateInitializer(Method m, List<Expression> dims);
}
