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
import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.Error;
import com.google.devtools.simple.compiler.expressions.ConstantExpression;
import com.google.devtools.simple.compiler.expressions.Expression;
import com.google.devtools.simple.compiler.types.Type;
import com.google.devtools.simple.classfiles.Method;
import com.google.devtools.simple.compiler.types.synthetic.ErrorType;

/**
 * Symbol used for constant data members.
 * 
 * @author Herbert Czymontek
 */
public final class ConstantDataMemberSymbol extends ObjectDataMemberSymbol {

  // Constant associated with the data member
  private Expression constExpr;

  /**
   * Creates a new constant data member symbol.
   * 
   * @param position  source code start position of symbol 
   * @param objectSymbol  defining object
   * @param name  data member name
   * @param type  data member type
   * @param constExpr  constant associated with the data member
   */
  public ConstantDataMemberSymbol(long position, ObjectSymbol objectSymbol, String name, Type type,
      Expression constExpr) {
    super(position, objectSymbol, name, type);
    this.constExpr = constExpr;
  }

  /**
   * Returns the value of the constant data member.
   * 
   * @return  constant value
   */
  public Expression getConstant() {
    return constExpr;
  }
  
  @Override
  public void resolve(Compiler compiler, FunctionSymbol currentFunction) {
    getType().resolve(compiler);
    constExpr = constExpr.resolve(compiler, currentFunction).checkType(compiler, getType());

    // Constant value must really be a constant
    if (!(constExpr instanceof ConstantExpression) &&
        !constExpr.getType().equals(ErrorType.errorType)) {
      compiler.error(constExpr.getPosition(), Error.errConstantValueExpected);
    }
  }

  @Override
  public void generateRead(Method m) {
    constExpr.generate(m);
  }

  @Override
  public void generateWrite(Method m) {
    Compiler.internalError();  // COV_NF_LINE
  }

  @Override
  public void generate(ClassFile cf) {
    Field field = cf.newField((short) (Field.ACC_PUBLIC | Field.ACC_FINAL | Field.ACC_STATIC),
        getName(), getType().signature());

    ((ConstantExpression) constExpr).generate(field);
  }

  @Override
  public String toString() {
    return "Const " + getName() + " As " + getType().toString();  // COV_NF_LINE
  }
}
