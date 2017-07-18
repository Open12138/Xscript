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

import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.classfiles.Method;

/**
 * Represents the primitive Single type.
 * 
 * @author Herbert Czymontek
 */
public final class SingleType extends FloatNumberType {

  /**
   * Instance of primitive Single type. 
   */
  public static final SingleType singleType = new SingleType();

  // Internal name of runtime support class for Double variants
  private static final String SINGLE_VARIANT_INTERNAL_NAME =
      VariantType.VARIANT_PACKAGE + "/SingleVariant";

  // Internal name of runtime support class for Single reference parameters
  private static final String SINGLE_REFERENCE_INTERNAL_NAME =
    REFERENCE_PACKAGE_INTERNAL_NAME + "/SingleReferenceParameter";

  /*
   * Creates a new Single type.
   */
  private SingleType() {
    super(TypeKind.SINGLE);
  }

  @Override
  public void generateDefaultInitializationValue(Method m) {
    m.generateInstrLdc(0f);
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
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "single2boolean", "(F)Z");
        break;
  
      case BYTE:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "single2byte", "(F)B");
        break;
  
      case SHORT:
        m.generateInstrInvokestatic(CONVERSION_HELPERS_INTERNAL_NAME, "single2short", "(F)S");
        break;
  
      case INTEGER:
        m.generateInstrF2i();
        break;
  
      case LONG:
        m.generateInstrF2l();
        break;
  
      case SINGLE:
        break;
  
      case DOUBLE:
        m.generateInstrF2d();
        break;
  
      case STRING:
        generateToString(m);
        break;
  
      case VARIANT:
        m.generateInstrInvokestatic(SINGLE_VARIANT_INTERNAL_NAME, "getSingleVariant",
            "(F)L" + SINGLE_VARIANT_INTERNAL_NAME + ";");
        break;
    }
  }

  @Override
  public void generateToString(Method m) {
    m.generateInstrInvokestatic("java/lang/Float", "toString", "(F)Ljava/lang/String;");
  }

  @Override
  public void generateLoadLocal(Method m, short varIndex) {
    m.generateInstrFload(varIndex);
  }

  @Override
  public void generateStoreLocal(Method m, short varIndex) {
    m.generateInstrFstore(varIndex);
  }

  @Override
  public void generateLoadArray(Method m) {
    m.generateInstrFaload();
  }

  @Override
  public void generateStoreArray(Method m) {
    m.generateInstrFastore();
  }

  @Override
  public void generateConst1(Method m) {
    m.generateInstrLdc(1F);
  }

  @Override
  public void generateConstM1(Method m) {
    m.generateInstrLdc(-1F);
  }

  @Override
  public void generateReturn(Method m) {
    m.generateInstrFreturn();
  }

  @Override
  public void generateNegation(Method m) {
    m.generateInstrFneg();
  }

  @Override
  public void generateAddition(Method m) {
    m.generateInstrFadd();
  }

  @Override
  public void generateSubtraction(Method m) {
    m.generateInstrFsub();
  }

  @Override
  public void generateMultiplication(Method m) {
    m.generateInstrFmul();
  }

  @Override
  public void generateIntegerDivision(Method m) {
    m.generateInstrInvokestatic(EXPRESSION_HELPERS_INTERNAL_NAME, "idiv", "(FF)F");
  }

  @Override
  public void generateModulo(Method m) {
    m.generateInstrFrem();
  }

  @Override
  public void generateExponentiation(Method m) {
    m.generateInstrInvokestatic(EXPRESSION_HELPERS_INTERNAL_NAME, "pow", "(FF)F");
  }

  @Override
  public void generateBranchIfCmpEqual(Method m, Method.Label label) {
    m.generateInstrFcmpg();
    m.generateInstrIfeq(label);
  }

  @Override
  public void generateBranchIfCmpNotEqual(Method m, Method.Label label) {
    m.generateInstrFcmpg();
    m.generateInstrIfne(label);
  }

  @Override
  public void generateBranchIfCmpLess(Method m, Method.Label label) {
    m.generateInstrFcmpg();
    m.generateInstrIflt(label);
  }

  @Override
  public void generateBranchIfCmpLessOrEqual(Method m, Method.Label label) {
    m.generateInstrFcmpg();
    m.generateInstrIfle(label);
  }

  @Override
  public void generateBranchIfCmpGreater(Method m, Method.Label label) {
    m.generateInstrFcmpg();
    m.generateInstrIfgt(label);
  }

  @Override
  public void generateBranchIfCmpGreaterOrEqual(Method m, Method.Label label) {
    m.generateInstrFcmpg();
    m.generateInstrIfge(label);
  }

  @Override
  public void generateJavaBox(Method m) {
    m.generateInstrInvokestatic("java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
  }

  @Override
  public String internalName() {
    return "F";
  }

  @Override
  public String referenceInternalName() {
    return SINGLE_REFERENCE_INTERNAL_NAME;
  }

  @Override
  public String referenceValueSignature() {
    return "F";
  }

  @Override
  public String toString() {
    return "Single";
  }
}
