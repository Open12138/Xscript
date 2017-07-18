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

package com.google.devtools.simple.compiler.expressions;

import com.google.devtools.simple.compiler.Error;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.compiler.types.IntegerType;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.types.ArrayType;
import com.google.devtools.simple.compiler.types.ObjectType;
import com.google.devtools.simple.compiler.types.Type;

import java.util.List;

/**
 * Represents object and array instantiation expressions.
 *
 * @author Herbert Czymontek
 */
public final class NewExpression extends Expression {

  // Dimensions for New array expressions (otherwise null)
  List<Expression> dimensions;

  /**
   * Creates a new object or array instantiation expression.
   * 
   * @param position  source code start position of expression
   * @param type  type of object or array to instantiate
   * @param dimensions  dimensions for New array (otherwise {@code null}
   */
  public NewExpression(long position, Type type, List<Expression> dimensions) {
    super(position);

    this.type = type;
    this.dimensions = dimensions;
  }

  @Override
  public Expression resolve(Compiler compiler, FunctionSymbol currentFunction) {
    type.resolve(compiler);

    if (dimensions != null) {
      // Resolve dimensions for array
      if (!type.isArrayType()) {
        Compiler.internalError();  // COV_NF_LINE
      }

      int argDimensions = dimensions.size();
      int arrayDimensions = ((ArrayType) type).getDimensions(); 
      if (arrayDimensions != argDimensions) {
        compiler.error(getPosition(),
            argDimensions > arrayDimensions ? Error.errTooManyIndices : Error.errTooFewIndices);
      }

      for (int i = 0; i < dimensions.size(); i++) {
        Expression dimension = dimensions.get(i);
        if (dimension == null) {
          compiler.error(getPosition(), Error.errArrayDimensionExpected);
        } else {
          dimensions.set(i, dimension.resolve(compiler, currentFunction).checkType(compiler,
              IntegerType.integerType));
        }
      }
    } else if (!type.isObjectType()) {
      compiler.error(getPosition(), Error.errObjectOrArrayTypeNeeded);
    }

    return this;
  }

  @Override
  public void generate(Method m) {
    if (type.isArrayType()) {
      ArrayType arrayType = (ArrayType) type;
      for (Expression dimension : dimensions) {
        dimension.generate(m);
      }
      arrayType.generateAllocateArray(m);
    } else {
      m.generateInstrNew(type.internalName());
      m.generateInstrDup();
      m.generateInstrInvokespecial(type.internalName(), "<init>", "()V");
      // With the exception of forms we always invoke the property initializer immediately after the
      // constructor. This way properties will be initialized correctly regardless of  whether
      // the object was defined in Java or Simple. Forms need to be treated differently because
      // their initialization sequence is more complicated (see comments in ConstructorSymbol
      // class).
      if (!((ObjectType) type).getObjectSymbol().isForm()) {
        m.generateInstrDup();
        m.generateInstrInvokestatic(Compiler.RUNTIME_ROOT_INTERNAL + "/Objects",
            "initializeProperties", "(Ljava/lang/Object;)V");
      }
    }
  }

  @Override
  public String toString() {
    return "New " + type.toString();  // COV_NF_LINE
  }
}
