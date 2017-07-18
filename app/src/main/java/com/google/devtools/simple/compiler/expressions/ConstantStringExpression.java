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

import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.compiler.types.StringType;
import com.google.devtools.simple.classfiles.Field;
import com.google.devtools.simple.classfiles.Method;

/**
 * This class represents constant expressions of strings.
 * 
 * @author Herbert Czymontek
 */
public final class ConstantStringExpression extends ConstantExpression {

  // Constant string value
  protected String value;

  /**
   * Creates a new constant expression for strings.
   * 
   * @param position  source code start position of expression
   * @param value  constant string value
   */
  public ConstantStringExpression(long position, String value) {
    super(position);

    this.value = value;
  }

  @Override
  public Expression resolve(Compiler compiler, FunctionSymbol currentFunction) {
    type = StringType.stringType;
    return this;
  }

  @Override
  public void generate(Method m) {
    m.generateInstrLdc(value);
  }

  @Override
  public void generate(Field f) {
    f.setConstantStringValue(value);
  }

  @Override
  public String toString() {
    return '\"' + value + '\"';  // COV_NF_LINE
  }
}
