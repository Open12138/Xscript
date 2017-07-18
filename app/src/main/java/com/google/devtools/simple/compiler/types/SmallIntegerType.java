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

package com.google.devtools.simple.compiler.types;

import com.google.devtools.simple.classfiles.Method;

/**
 * Superclass for all small (32-bit or smaller) integer types.
 * 
 * @author Herbert Czymontek
 */
public abstract class SmallIntegerType extends IntegerNumberType {

  /**
   * Creates a new small integer type.
   * 
   * @param kind  type kind
   */
  protected SmallIntegerType(TypeKind kind) {
    super(kind);
  }

  @Override
  public void generateDefaultInitializationValue(Method m) {
    m.generateInstrLdc(0);
  }

  @Override
  public void generateLoadLocal(Method m, short varIndex) {
    m.generateInstrIload(varIndex);
  }

  @Override
  public void generateStoreLocal(Method m, short varIndex) {
    m.generateInstrIstore(varIndex);
  }

  @Override
  public void generateConst1(Method m) {
    m.generateInstrLdc(1);
  }

  @Override
  public void generateConstM1(Method m) {
    m.generateInstrLdc(-1);
  }

  @Override
  public void generateReturn(Method m) {
    m.generateInstrIreturn();
  }

  @Override
  public void generateNegation(Method m) {
    m.generateInstrIneg();
  }

  @Override
  public void generateAddition(Method m) {
    m.generateInstrIadd();
  }

  @Override
  public void generateSubtraction(Method m) {
    m.generateInstrIsub();
  }

  @Override
  public void generateMultiplication(Method m) {
    m.generateInstrImul();
  }

  @Override
  public void generateIntegerDivision(Method m) {
    m.generateInstrIdiv();
  }

  @Override
  public void generateModulo(Method m) {
    m.generateInstrIrem();
  }

  @Override
  public void generateExponentiation(Method m) {
    m.generateInstrInvokestatic(EXPRESSION_HELPERS_INTERNAL_NAME, "pow", "(II)I");
  }

  @Override
  public void generateBitNot(Method m) {
    generateConstM1(m);
    m.generateInstrIxor();
  }

  @Override
  public void generateBitAnd(Method m) {
    m.generateInstrIand();
  }

  @Override
  public void generateBitOr(Method m) {
    m.generateInstrIor();
  }

  @Override
  public void generateBitXor(Method m) {
    m.generateInstrIxor();
  }

  @Override
  public void generateShiftLeft(Method m) {
    m.generateInstrIshl();
  }

  @Override
  public void generateShiftRight(Method m) {
    m.generateInstrIushr();
  }

  @Override
  public void generateBranchIfCmpEqual(Method m, Method.Label label) {
    m.generateInstrIfIcmpeq(label);
  }

  @Override
  public void generateBranchIfCmpNotEqual(Method m, Method.Label label) {
    m.generateInstrIfIcmpne(label);
  }

  @Override
  public void generateBranchIfCmpLess(Method m, Method.Label label) {
    m.generateInstrIfIcmplt(label);
  }

  @Override
  public void generateBranchIfCmpLessOrEqual(Method m, Method.Label label) {
    m.generateInstrIfIcmple(label);
  }

  @Override
  public void generateBranchIfCmpGreater(Method m, Method.Label label) {
    m.generateInstrIfIcmpgt(label);
  }

  @Override
  public void generateBranchIfCmpGreaterOrEqual(Method m, Method.Label label) {
    m.generateInstrIfIcmpge(label);
  }

  @Override
  public String signature() {
    return internalName();
  }
}
