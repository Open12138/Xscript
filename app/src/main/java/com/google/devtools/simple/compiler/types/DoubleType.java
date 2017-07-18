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
import com.google.devtools.simple.compiler.Compiler;

/**
 * Represents the primitive Double type.
 * 
 * @author Herbert Czymontek
 */
public final class DoubleType extends FloatNumberType {

  /**
   * Instance of primitive Double type. 
   */
  public static final DoubleType doubleType = new DoubleType();

  // Internal name of runtime support class for Double variants
  private static final String DOUBLE_VARIANT_INTERNAL_NAME =
      VariantType.VARIANT_PACKAGE + "/DoubleVariant";

  // Internal name of runtime support class for Double reference parameters
  private static final String DOUBLE_REFERENCE_INTERNAL_NAME =
      REFERENCE_PACKAGE_INTERNAL_NAME + "/DoubleReferenceParameter";

  /*
   * Creates a new Double type.
   */
  private DoubleType() {
    super(TypeKind.DOUBLE);
  }

  @Override
  public boolean isWideType() {
    return true;
  }

  @Override
  public void generateDefaultInitializationValue(Method m) {
    m.generateInstrLdc2(0d);
  }

  @Override
  public void generateConversion(Method m, Type toType) {
    switch (toType.getKind()) {
      default:
        // COV_NF_START
        Compiler.internalError();
        break;
        // COV_NF_END
  
      case BOOLEAN:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "double2boolean", "(D)Z");
        break;
  
      case BYTE:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "double2byte", "(D)B");
        break;
  
      case SHORT:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "double2short", "(D)S");
        break;
  
      case INTEGER:
        m.generateInstrD2i();
        break;
  
      case LONG:
        m.generateInstrD2l();
        break;
  
      case SINGLE:
        m.generateInstrD2f();
        break;
  
      case DOUBLE:
        break;
  
      case STRING:
        generateToString(m);
        break;
  
      case VARIANT:
        m.generateInstrInvokestatic(DOUBLE_VARIANT_INTERNAL_NAME, "getDoubleVariant",
            "(D)L" + DOUBLE_VARIANT_INTERNAL_NAME + ";");
        break;
    }
  }

  @Override
  public void generateToString(Method m) {
    m.generateInstrInvokestatic("java/lang/Double", "toString", "(D)Ljava/lang/String;");
  }

  @Override
  public void generateLoadLocal(Method m, short varIndex) {
    m.generateInstrDload(varIndex);
  }

  @Override
  public void generateStoreLocal(Method m, short varIndex) {
    m.generateInstrDstore(varIndex);
  }

  @Override
  public void generateLoadArray(Method m) {
    m.generateInstrDaload();
  }

  @Override
  public void generateStoreArray(Method m) {
    m.generateInstrDastore();
  }

  @Override
  public void generateConst1(Method m) {
    m.generateInstrLdc2(1D);
  }

  @Override
  public void generateConstM1(Method m) {
    m.generateInstrLdc2(-1D);
  }

  @Override
  public void generateReturn(Method m) {
    m.generateInstrDreturn();
  }

  @Override
  public void generatePop(Method m) {
    m.generateInstrPop2();
  }

  @Override
  public void generateNegation(Method m) {
    m.generateInstrDneg();
  }

  @Override
  public void generateAddition(Method m) {
    m.generateInstrDadd();
  }

  @Override
  public void generateSubtraction(Method m) {
    m.generateInstrDsub();
  }

  @Override
  public void generateMultiplication(Method m) {
    m.generateInstrDmul();
  }

  @Override
  public void generateDivision(Method m) {
    m.generateInstrDdiv();
  }

  @Override
  public void generateIntegerDivision(Method m) {
    m.generateInstrInvokestatic(EXPRESSION_HELPERS_INTERNAL_NAME, "idiv", "(DD)D");
  }

  @Override
  public void generateModulo(Method m) {
    m.generateInstrDrem();
  }

  @Override
  public void generateExponentiation(Method m) {
    m.generateInstrInvokestatic("java/lang/Math", "pow", "(DD)D");
  }

  @Override
  public void generateBranchIfCmpEqual(Method m, Method.Label label) {
    m.generateInstrDcmpg();
    m.generateInstrIfeq(label);
  }

  @Override
  public void generateBranchIfCmpNotEqual(Method m, Method.Label label) {
    m.generateInstrDcmpg();
    m.generateInstrIfne(label);
  }

  @Override
  public void generateBranchIfCmpLess(Method m, Method.Label label) {
    m.generateInstrDcmpg();
    m.generateInstrIflt(label);
  }

  @Override
  public void generateBranchIfCmpLessOrEqual(Method m, Method.Label label) {
    m.generateInstrDcmpg();
    m.generateInstrIfle(label);
  }

  @Override
  public void generateBranchIfCmpGreater(Method m, Method.Label label) {
    m.generateInstrDcmpg();
    m.generateInstrIfgt(label);
  }

  @Override
  public void generateBranchIfCmpGreaterOrEqual(Method m, Method.Label label) {
    m.generateInstrDcmpg();
    m.generateInstrIfge(label);
  }

  @Override
  public void generateJavaBox(Method m) {
    m.generateInstrInvokestatic("java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
  }

  @Override
  public String internalName() {
    return "D";
  }

  @Override
  public String referenceInternalName() {
    return DOUBLE_REFERENCE_INTERNAL_NAME;
  }

  @Override
  public String referenceValueSignature() {
    return "D";
  }

  @Override
  public String toString() {
    return "Double";
  }
}
